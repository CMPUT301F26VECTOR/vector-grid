#!/usr/bin/env python3
"""
Turns the KDoc comments in the app's Kotlin source into ONE GitHub wiki page,
Code-Documentation.md.

GitHub wikis only show Markdown, and Dokka 2 only makes HTML, so this reads the
same /** ... */ comments Dokka reads. The page has a contents list, then the
source files grouped into sections (Models and logic, Firebase and data access,
Screens and UI, Theme). Each file shows a one-line summary, and its classes,
functions and properties (signature + KDoc) sit in a collapsible block so the
page stays short. Like Dokka's defaults, private and internal declarations are
left out.

Run by .github/workflows/wiki-code-docs.yml on every push to main.

Usage: kdoc_to_wiki.py <kotlin source root> <wiki folder> <owner/repo> [branch]
"""
import os
import re
import sys

DOKKA_SITE = "https://cmput301f26vector.github.io/vector-grid/"
OLD_PAGE_PREFIX = "Docs-"  # per-file pages made by an earlier version; removed
INDEX_PAGE = "Code-Documentation"

DECL_RE = re.compile(
    r"^(?:(?:public|protected|open|abstract|sealed|data|enum|inner|override|"
    r"const|lateinit|suspend|inline|operator|infix|tailrec|external|annotation|"
    r"value|companion)\s+)*"
    r"(class|interface|object|fun|val|var|typealias)\b\s*(.*)$"
)
def read_kdoc(lines, i):
    """Collects a /** ... */ block that starts at line i. Returns (text lines, next index)."""
    body = []
    while i < len(lines):
        line = lines[i]
        done = "*/" in line
        line = line.split("*/")[0]
        if line.lstrip().startswith("/**"):
            line = re.sub(r"^\s*/\*\*\s?", "", line)
        else:
            line = re.sub(r"^\s*\* ?", "", line)
        body.append(line.rstrip())
        i += 1
        if done:
            break
    while body and not body[0].strip():
        body.pop(0)
    while body and not body[-1].strip():
        body.pop()
    return body, i


def read_declaration(lines, i):
    """Finds the declaration after a KDoc (skipping blank lines and annotations).
    Returns (indent, signature text, next index) or (None, None, i)."""
    while i < len(lines) and (not lines[i].strip() or lines[i].strip().startswith("@")):
        # an annotation can have arguments over several lines
        if lines[i].strip().startswith("@") and lines[i].count("(") > lines[i].count(")"):
            depth = lines[i].count("(") - lines[i].count(")")
            i += 1
            while i < len(lines) and depth > 0:
                depth += lines[i].count("(") - lines[i].count(")")
                i += 1
            continue
        i += 1
    if i >= len(lines):
        return None, None, i
    first = lines[i]
    indent = len(first) - len(first.lstrip())
    if first.strip().startswith(("//", "/*", "package", "import")):
        return None, None, i

    parts, depth = [], 0
    while i < len(lines):
        text = lines[i].split("//")[0].rstrip()
        parts.append(text.strip())
        depth += text.count("(") - text.count(")")
        i += 1
        if depth <= 0 and ("{" in text or "=" in text or not text.endswith((",", "(", ":"))):
            break
        if len(parts) > 25:
            break
    sig = " ".join(p for p in parts if p)
    sig = cut_body(sig)
    sig = re.sub(r"\(\s+", "(", sig)
    sig = re.sub(r"\s+\)", ")", sig)
    sig = re.sub(r",\s*\)", ")", sig)
    sig = re.sub(r"\s+", " ", sig).strip()
    return indent, sig, i


def cut_body(sig):
    """Removes the body: everything from the first '{' or top-level '=' after the parameters."""
    depth = 0
    for k, ch in enumerate(sig):
        if ch in "(<[":
            depth += 1
        elif ch in ")>]" and sig[k - 1:k + 1] != "->":
            depth -= 1
        elif depth <= 0 and ch == "{":
            return sig[:k].rstrip()
        elif depth <= 0 and ch == "=" and sig[k - 1:k + 1] not in ("!=", "==", "<=", ">=") \
                and sig[k:k + 2] not in ("==", "=>"):
            return sig[:k].rstrip()
    return sig


def format_sig(sig):
    """Puts each parameter of a long signature on its own line, like Dokka does."""
    if len(sig) <= 100 or "(" not in sig:
        return sig
    start = sig.index("(")
    depth, end, params, last = 0, None, [], start + 1
    for k in range(start, len(sig)):
        ch = sig[k]
        if ch in "(<[":
            depth += 1
        elif ch in ")>]" and sig[k - 1:k + 1] != "->":
            depth -= 1
            if depth == 0:
                end = k
                break
        elif ch == "," and depth == 1:
            params.append(sig[last:k].strip())
            last = k + 1
    if end is None:
        return sig
    params.append(sig[last:end].strip())
    params = [p for p in params if p]
    return sig[:start + 1] + "\n" + ",\n".join("    " + p for p in params) + "\n" + sig[end:]


def parse_tags(body):
    """Splits KDoc text into the description and its @tags."""
    desc, tags, current = [], [], None
    for line in body:
        m = re.match(r"^@(\w+)\s*(\S*)\s*(.*)$", line.strip())
        if m:
            tag, arg, rest = m.groups()
            if tag in ("return", "since", "author", "see", "sample", "constructor"):
                rest = (arg + " " + rest).strip()
                arg = ""
            current = [tag, arg, rest]
            tags.append(current)
        elif current is not None:
            current[2] = (current[2] + " " + line.strip()).strip()
        else:
            desc.append(line)
    return desc, tags


def md_lines(lines):
    """Description lines to Markdown: indented runs become a code block so their
    layout is kept; everything else is normal text."""
    out, block = [], []
    for line in lines + [""]:
        if line.startswith("  ") and line.strip():
            block.append(line)
            continue
        if block:
            out += ["```"] + block + ["```"]
            block = []
        out.append(md_text(line))
    return out[:-1]


def md_text(text):
    """KDoc [Links] become code, as there are no pages for them here."""
    return re.sub(r"\[([A-Za-z_][\w.]*)\]", r"`\1`", text)


def name_of(kind, rest):
    rest = re.sub(r"^<[^>]*>\s*", "", rest)  # generic type parameters
    m = re.match(r"(?:[\w.<>?, ]+\.)?`?([\w]+)`?", rest)
    return m.group(1) if m else rest.split()[0] if rest else kind


def render_entry(out, level, kind, name, sig, body):
    out.append(f"{'#' * level} {kind} {name}")
    out.append("")
    out.append("```kotlin")
    out.append(format_sig(sig))
    out.append("```")
    out.append("")
    desc, tags = parse_tags(body)
    if any(l.strip() for l in desc):
        out.extend(md_lines(desc))
        out.append("")
    labels = {"param": "Parameters", "property": "Properties", "return": "Returns",
              "throws": "Throws", "exception": "Throws", "see": "See also"}
    for tag in ["property", "param", "return", "throws", "exception", "see"]:
        items = [t for t in tags if t[0] == tag]
        if not items:
            continue
        out.append(f"**{labels[tag]}**")
        out.append("")
        for _, arg, text in items:
            out.append(f"- `{arg}`: {md_text(text)}" if arg else f"- {md_text(text)}")
        out.append("")


SECTIONS = [
    ("Models and logic", "Classes that hold the app's data and rules. They have no screens."),
    ("Firebase and data access", "Code that signs users in and reads or writes Firestore."),
    ("Screens and UI", "Jetpack Compose screens, cards and the activities that show them."),
    ("Theme", "Colours, text styles and the Material theme."),
]


def section_of(rel):
    stem = os.path.splitext(os.path.basename(rel))[0]
    if "/theme/" in "/" + rel.replace("\\", "/"):
        return "Theme"
    if re.search(r"Screen|UI|View|Activity|Gate", stem):
        return "Screens and UI"
    if re.search(r"Repository|Firebase|Auth", stem):
        return "Firebase and data access"
    return "Models and logic"


def anchor(heading):
    """The link target GitHub gives a heading."""
    a = heading.strip().lower()
    a = re.sub(r"[^\w\- ]", "", a)
    return a.replace(" ", "-")


def parse_file(path):
    with open(path, encoding="utf-8") as f:
        lines = f.read().splitlines()
    entries = []  # (indent, kind, name, sig, body)
    i = 0
    while i < len(lines):
        if lines[i].lstrip().startswith("/**"):
            body, i = read_kdoc(lines, i)
            indent, sig, i = read_declaration(lines, i)
            if sig is None:
                entries.append((0, "file", "", "", body))
                continue
            # like Dokka's defaults: leave out private and internal declarations
            if re.match(r"^(?:\w+\s+)*?(private|internal)\b", sig.split("(")[0]):
                continue
            m = DECL_RE.match(sig)
            if not m:
                continue
            kind, rest = m.group(1), m.group(2)
            if "enum class" in sig:
                kind = "enum class"
            elif "data class" in sig:
                kind = "data class"
            elif sig.startswith("companion object") or " companion object" in sig:
                kind, rest = "companion object", "Companion"
            entries.append((indent, kind, name_of(kind, rest), sig, body))
        else:
            i += 1
    return entries


def summary_of(entries):
    for _, _, _, _, body in entries:
        desc, _ = parse_tags(body)
        first = []  # first paragraph only, stopping before any list
        for l in desc:
            if not l.strip() or l.strip().startswith("- "):
                if first:
                    break
                continue
            first.append(l.strip())
        text = " ".join(first)
        if text:
            return md_text(re.split(r"(?<=[.!?])\s", text, maxsplit=1)[0].rstrip(":"))
    return "_No KDoc yet._"


def main():
    if len(sys.argv) < 4:
        sys.exit(__doc__)
    src, wiki, repo = sys.argv[1], sys.argv[2], sys.argv[3]
    branch = sys.argv[4] if len(sys.argv) > 4 else "main"

    files = []
    for root, _, names in os.walk(src):
        for n in names:
            if n.endswith(".kt"):
                full = os.path.join(root, n)
                files.append((full, os.path.relpath(full, src)))
    files.sort(key=lambda f: os.path.basename(f[1]).lower())

    grouped = {name: [] for name, _ in SECTIONS}
    for full, rel in files:
        entries = parse_file(full)
        grouped[section_of(rel)].append((rel, entries))

    total = sum(1 for g in grouped.values() for _, es in g for e in es if e[1] != "file")
    out = [
        "# Code Documentation", "",
        f"Documentation for all {len(files)} Kotlin source files in the app "
        f"({total} documented classes, functions and properties), taken from the KDoc "
        "comments in the code. This page is rebuilt automatically on every push to `main` "
        "by `.github/workflows/wiki-code-docs.yml`, so it always matches the current code. "
        "To change it, edit the comments in the code, not this page.", "",
        f"The same documentation as a full Dokka website: **{DOKKA_SITE}**", "",
        "## Contents", "",
    ]
    for name, _ in SECTIONS:
        if not grouped[name]:
            continue
        out.append(f"- [{name}](#{anchor(name)})")
        for rel, entries in grouped[name]:
            fname = os.path.basename(rel)
            out.append(f"  - [{fname}](#{anchor(fname)}): {summary_of(entries)}")
    out.append("")

    for name, blurb in SECTIONS:
        if not grouped[name]:
            continue
        out += ["---", "", f"## {name}", "", f"_{blurb}_", ""]
        for rel, entries in grouped[name]:
            fname = os.path.basename(rel)
            url = f"https://github.com/{repo}/blob/{branch}/Project_halfway/app/src/main/java/{rel}"
            decls = [e for e in entries if e[1] != "file"]
            out += [f"### {fname}", "", f"{summary_of(entries)} ([source]({url}))", ""]
            if not entries:
                continue
            out += ["<details>",
                    f"<summary>Show details ({len(decls)} documented declaration"
                    f"{'' if len(decls) == 1 else 's'})</summary>", ""]
            for indent, kind, dname, sig, body in entries:
                if kind == "file":
                    desc, _ = parse_tags(body)
                    out += md_lines(desc) + [""]
                    continue
                render_entry(out, 4 if indent == 0 else 5, kind, dname, sig, body)
            out += ["</details>", ""]
        out += [f"[Back to contents](#contents)", ""]

    with open(os.path.join(wiki, INDEX_PAGE + ".md"), "w", encoding="utf-8") as f:
        f.write("\n".join(out).rstrip() + "\n")

    # remove the per-file pages an earlier version of this script made
    removed = 0
    for old in os.listdir(wiki):
        if old.startswith(OLD_PAGE_PREFIX) and old.endswith(".md"):
            os.remove(os.path.join(wiki, old))
            removed += 1
    print(f"Wrote {INDEX_PAGE}.md ({len(files)} files, {total} declarations); "
          f"removed {removed} old per-file pages")


if __name__ == "__main__":
    main()

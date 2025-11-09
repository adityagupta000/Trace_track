import os
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.pdfgen import canvas


def get_code_files(directory, excluded_files=None, excluded_dirs=None):
    """Fetch all Java backend files excluding build artifacts and sensitive files."""
    if excluded_files is None:
        # Excluded files
        excluded_files = {
            ".DS_Store",
            "Thumbs.db",
            "Desktop.ini",
            # All .env files
            ".env",
            ".env.development",
            ".env.production",
            ".env.local",
            ".env.staging",
            ".env.test",
            ".env.example",
            # Maven wrapper files
            "maven-wrapper.properties",
            "mvnw",
            "mvnw.cmd",
            # Git files
            ".gitignore",
            ".gitattributes",
            # Documentation
            "HELP.md",
            "README.md",
        }

    if excluded_dirs is None:
        excluded_dirs = {
            "node_modules",
            ".git",
            "__pycache__",
            "build",
            "dist",
            ".next",
            "coverage",
            ".nyc_output",
            "logs",
            "uploads",  # Contains user-uploaded files
            "images",  # Contains image assets
            "public",  # Contains only static assets
            "target",  # Maven build output
            ".mvn",  # Maven wrapper
            "generated-sources",
            "generated-test-sources",
            "maven-archiver",
            "maven-status",
            "test-classes",  # Compiled test classes
        }

    backend_files = {}

    # Define Java file extensions only
    java_extensions = {".java"}

    for root, dirs, files in os.walk(directory):
        # Skip excluded directories
        dirs[:] = [d for d in dirs if d not in excluded_dirs]

        # Skip if current directory is an excluded directory
        if any(excluded_dir in root.split(os.sep) for excluded_dir in excluded_dirs):
            continue

        for file in files:
            # Skip excluded files
            if file in excluded_files:
                continue

            # Additional check for any file containing .env pattern
            if ".env" in file:
                continue

            file_path = os.path.join(root, file)

            # Get file extension
            _, ext = os.path.splitext(file)

            # Include Java files only
            if ext.lower() in java_extensions:
                try:
                    with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
                        content = f.readlines()

                        # Check if it's in src/main/java (backend code only)
                        if os.path.join("src", "main", "java") in file_path:
                            backend_files[file_path] = content

                except Exception as e:
                    print(f"❌ Error reading {file_path}: {e}")
                    backend_files[file_path] = [f"[Error reading file: {str(e)}]"]

    return backend_files


def create_pdf(code_data, output_pdf="Backend_Code_Export.pdf"):
    c = canvas.Canvas(output_pdf, pagesize=A4)
    width, height = A4
    margin = 15 * mm
    line_height = 9
    y = height - margin
    max_line_width = 95  # Characters per line for wrapping

    # Title
    c.setFont("Helvetica-Bold", 16)
    c.drawString(margin, y, f"📁 Lost & Found Backend - Java Code Export")
    y -= 2 * line_height
    c.setFont("Helvetica-Bold", 12)
    c.drawString(margin, y, f"📁 Backend Java Files:")
    y -= 2 * line_height

    file_paths = sorted(list(code_data.keys()))

    # 1. File list with structure
    c.setFont("Courier", 7)

    for path in file_paths:
        if y < margin:
            c.showPage()
            c.setFont("Courier", 7)
            y = height - margin

        display_path = os.path.relpath(path)
        c.drawString(margin, y, f"  [JAVA] {display_path}")
        y -= line_height

    # Add page break before code content
    c.showPage()
    y = height - margin

    # 2. File contents with word wrapping
    for file_path in file_paths:
        lines = code_data[file_path]
        print(f"📄 Adding: {file_path}")

        if y < margin + 3 * line_height:
            c.showPage()
            y = height - margin

        # File header
        rel_path = os.path.relpath(file_path)
        c.setFont("Helvetica-Bold", 10)
        c.drawString(margin, y, f"📄 File: {rel_path}")
        y -= line_height

        # Add separator line
        c.setFont("Courier", 7)
        c.drawString(margin, y, "=" * max_line_width)
        y -= line_height

        # File content with line numbers and wrapping
        for line_num, line in enumerate(lines, 1):
            # Clean the line
            line = line.rstrip("\n").encode("latin-1", "replace").decode("latin-1")
            
            # Skip empty lines but show them
            if not line.strip():
                if y < margin:
                    c.showPage()
                    c.setFont("Courier", 7)
                    y = height - margin
                c.drawString(margin, y, f"{line_num:3d}:")
                y -= line_height
                continue
            
            # Wrap long lines
            if len(line) <= max_line_width - 5:  # -5 for line number space
                if y < margin:
                    c.showPage()
                    c.setFont("Courier", 7)
                    y = height - margin
                display_line = f"{line_num:3d}: {line}"
                c.drawString(margin, y, display_line)
                y -= line_height
            else:
                # Split long lines into chunks
                remaining = line
                first_chunk = True
                while remaining:
                    if y < margin:
                        c.showPage()
                        c.setFont("Courier", 7)
                        y = height - margin
                    
                    if first_chunk:
                        chunk = remaining[:max_line_width - 5]
                        display_line = f"{line_num:3d}: {chunk}"
                        first_chunk = False
                    else:
                        chunk = remaining[:max_line_width - 5]
                        display_line = f"    | {chunk}"  # Continuation indicator
                    
                    c.drawString(margin, y, display_line)
                    y -= line_height
                    remaining = remaining[max_line_width - 5:]

        # Add spacing between files
        y -= line_height
        if y > margin:
            c.setFont("Courier", 7)
            c.drawString(margin, y, "-" * max_line_width)
            y -= 2 * line_height

    c.save()
    print(f"✅ PDF successfully created: {output_pdf}")
    print(f"📊 Total Java files processed: {len(code_data)}")


def main():
    root_dir = os.path.dirname(os.path.abspath(__file__))

    # Excluded files
    excluded_files = {
        ".DS_Store",
        "Thumbs.db",
        "Desktop.ini",
        ".env",
        ".env.development",
        ".env.production",
        ".env.local",
        ".env.staging",
        ".env.test",
        ".env.example",
        "maven-wrapper.properties",
        "mvnw",
        "mvnw.cmd",
        ".gitignore",
        ".gitattributes",
        "HELP.md",
        "README.md",
    }

    # Directories to exclude
    excluded_dirs = {
        "node_modules",
        ".git",
        "__pycache__",
        "build",
        "dist",
        ".next",
        "coverage",
        ".nyc_output",
        "logs",
        "uploads",
        "images",
        "public",
        "target",
        ".mvn",
        "generated-sources",
        "generated-test-sources",
        "maven-archiver",
        "maven-status",
        "test-classes",
    }

    print("🔍 Scanning for Java backend files...")
    print("🔒 Excluded: .env files, logs, uploads, images, target directory")
    print("=" * 60)

    backend_files = get_code_files(root_dir, excluded_files, excluded_dirs)

    if not backend_files:
        print("❌ No Java backend files found to process!")
        return

    print(f"\n📁 Found {len(backend_files)} backend Java files")

    # Show backend files
    print("\n📋 Backend Java Files to be included:")
    for file_path in sorted(backend_files.keys()):
        print(f"   📄 {os.path.relpath(file_path)}")

    print("\n" + "=" * 60)

    # Create Backend PDF
    print("\n📝 Generating Backend Code PDF...")
    create_pdf(backend_files)

    print("\n" + "=" * 60)
    print("✅ PDF generated successfully!")
    print(f"   📄 Backend_Code_Export.pdf ({len(backend_files)} files)")


if __name__ == "__main__":
    main()
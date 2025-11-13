import os
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.pdfgen import canvas


def get_code_files(directory, excluded_files=None, excluded_dirs=None):
    """Fetch all frontend files (React/JS/CSS) excluding build artifacts and dependencies."""
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
            # Git files
            ".gitignore",
            ".gitattributes",
            # Documentation
            "README.md",
            # Test files
            "setupTests.js",
            "App.test.js",
            "reportWebVitals.js",
            # Config files that don't contain business logic
            "postcss.config.js",
            # Lock files
            "package-lock.json",
            "yarn.lock",
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
            "uploads",
            "images",
            "public",  # Static assets only
            "target",
            ".mvn",
            "generated-sources",
            "generated-test-sources",
            "maven-archiver",
            "maven-status",
            "test-classes",
            "lost-and-found-backend",  # Exclude backend directory
        }

    frontend_files = {}

    # Define frontend file extensions
    frontend_extensions = {".js", ".jsx", ".css", ".json"}

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

            # Include frontend files only
            if ext.lower() in frontend_extensions:
                try:
                    with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
                        content = f.readlines()

                        # Include files from src/ directory and root config files
                        if (os.path.join("src") in file_path or 
                            file in ["package.json", "tailwind.config.js"]):
                            frontend_files[file_path] = content

                except Exception as e:
                    print(f"❌ Error reading {file_path}: {e}")
                    frontend_files[file_path] = [f"[Error reading file: {str(e)}]"]

    return frontend_files


def create_pdf(code_data, output_pdf="Frontend_Code_Export.pdf"):
    c = canvas.Canvas(output_pdf, pagesize=A4)
    width, height = A4
    margin = 15 * mm
    line_height = 9
    y = height - margin
    max_line_width = 95  # Characters per line for wrapping

    # Title
    c.setFont("Helvetica-Bold", 16)
    c.drawString(margin, y, f"📁 Lost & Found Frontend - React Code Export")
    y -= 2 * line_height
    c.setFont("Helvetica-Bold", 12)
    c.drawString(margin, y, f"📁 Frontend React/JS Files:")
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
        _, ext = os.path.splitext(path)
        file_type = ext.upper()[1:]  # Remove the dot
        c.drawString(margin, y, f"  [{file_type}] {display_path}")
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
    print(f"📊 Total frontend files processed: {len(code_data)}")


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
        ".gitignore",
        ".gitattributes",
        "README.md",
        "setupTests.js",
        "App.test.js",
        "reportWebVitals.js",
        "postcss.config.js",
        "package-lock.json",
        "yarn.lock",
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
        "lost-and-found-backend",
    }

    print("🔍 Scanning for frontend files (React/JS/CSS)...")
    print("🔒 Excluded: .env files, node_modules, build, public, test files")
    print("=" * 60)

    frontend_files = get_code_files(root_dir, excluded_files, excluded_dirs)

    if not frontend_files:
        print("❌ No frontend files found to process!")
        return

    print(f"\n📁 Found {len(frontend_files)} frontend files")

    # Show frontend files
    print("\n📋 Frontend Files to be included:")
    for file_path in sorted(frontend_files.keys()):
        print(f"   📄 {os.path.relpath(file_path)}")

    print("\n" + "=" * 60)

    # Create Frontend PDF
    print("\n📝 Generating Frontend Code PDF...")
    create_pdf(frontend_files)

    print("\n" + "=" * 60)
    print("✅ PDF generated successfully!")
    print(f"   📄 Frontend_Code_Export.pdf ({len(frontend_files)} files)")


if __name__ == "__main__":
    main()
from PIL import Image

# --- EDIT THESE PATHS ---
image_paths = ["Focus Start.png", "Focus.png", "Focus Get.png", "Focus End.png"]  # Your file names
output_path = "Focus Combined2.png"
# ---

# Load and convert images to support transparency
images = [Image.open(path).convert("RGBA") for path in image_paths]

# Calculate total width and maximum height
total_width = sum(img.width for img in images)
max_height = max(img.height for img in images)

# Create a blank transparent canvas
combined = Image.new("RGBA", (total_width, max_height), (0, 0, 0, 0))

# Paste images side-by-side (top-aligned)
x_offset = 0
for img in images:
    combined.paste(img, (x_offset, 0), img)  # The 'img' mask preserves transparency
    x_offset += img.width

combined.save(output_path, "PNG")
print(f"Success! Saved to {output_path}")
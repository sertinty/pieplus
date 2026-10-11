from PIL import Image, ImageDraw

S = 128
img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

# 背景
pad = 4
d.rounded_rectangle([pad, pad, S - pad - 1, S - pad - 1], radius=22,
                    fill=(18, 24, 34, 255), outline=(63, 169, 245, 255), width=3)

# 饼图本体
box = [24, 24, 104, 104]
slices = [
    (0, 70, (59, 130, 246, 255)),
    (70, 150, (96, 165, 250, 255)),
    (150, 230, (147, 197, 253, 255)),
    (230, 300, (37, 99, 235, 255)),
    (300, 360, (191, 219, 254, 255)),
]
for start, end, color in slices:
    d.pieslice(box, start, end, fill=color, outline=(18, 24, 34, 255), width=2)

# 中间留个深色圆心，像原版饼图
d.ellipse([52, 52, 76, 76], fill=(18, 24, 34, 255))

# 右下角 "10+"，表示序号可以超过 9
font = None
try:
    from PIL import ImageFont
    font = ImageFont.load_default()
except Exception:
    pass
tag = Image.new("RGBA", (34, 16), (0, 0, 0, 0))
td = ImageDraw.Draw(tag)
td.text((1, 4), "10+", font=font, fill=(255, 214, 102, 255))
tag = tag.resize((68, 32), Image.NEAREST)
img.alpha_composite(tag, (56, 88))

img.save(r"D:\sertinty\Documents\dsh\pieplus\src\main\resources\assets\pieplus\icon.png")
print("icon written")

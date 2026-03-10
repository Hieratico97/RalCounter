import codecs
import re

file_path = 'app/src/main/res/layout/fragment_combo.xml'
with codecs.open(file_path, 'r', 'utf-8') as f:
    content = f.read()

# Separate the content by the common decorative divider line used before every major section
pattern = r'([\s]*<!-- ═══════════════════════════════════════════\r?\n)'
parts = re.split(pattern, content)
# parts[0] is everything before the first divider. 
# Then it's pairs of (divider, content), so len(parts) should be 1 + 2*6 = 13.
# Let's verify:
# 0 = Top up to spacer
# 1 = Divider
# 2 = SPELL COUNTERS
# 3 = Divider
# 4 = CRIATURA
# 5 = Divider
# 6 = PLANESWALKER
# 7 = Divider
# 8 = LIFE TOTAL
# 9 = Divider
# 10 = MANA POOL
# 11 = Divider
# 12 = ACTION BUTTONS

if len(parts) == 13:
    print("Parts successfully parsed!")
    # Desired order:
    # Top chunk
    new_content = parts[0]
    
    # 1. LIFE TOTAL
    new_content += parts[7] + parts[8]
    # 2. MANA POOL
    new_content += parts[9] + parts[10]
    # 3. SPELL COUNTERS
    new_content += parts[1] + parts[2]
    # 4. CRIATURA
    new_content += parts[3] + parts[4]
    # 5. PLANESWALKER
    new_content += parts[5] + parts[6]
    # 6. ACTION BUTTONS
    new_content += parts[11] + parts[12]

    with codecs.open(file_path, 'w', 'utf-8') as f:
        f.write(new_content)
    print("File successfully reordered and overwritten.")
else:
    print("Error parsing parts. Length is:", len(parts))

import codecs
import re
import os

file_path = 'app/src/main/res/layout/fragment_combo.xml'
land_dir = 'app/src/main/res/layout-land'
land_file_path = os.path.join(land_dir, 'fragment_combo.xml')

if not os.path.exists(land_dir):
    os.makedirs(land_dir)

with codecs.open(file_path, 'r', 'utf-8') as f:
    content = f.read()

pattern = r'([\s]*<!-- ═══════════════════════════════════════════\r?\n)'
parts = re.split(pattern, content)

# Expected order in current vertical file:
# 0: Top chunk (FrameLayout + ScrollView + LinearLayout)
# 1, 2: LIFE TOTAL
# 3, 4: MANA POOL
# 5, 6: SPELL COUNTERS
# 7, 8: CRIATURA
# 9, 10: PLANESWALKER
# 11, 12: ACTION BUTTONS

if len(parts) == 13:
    print("Parts successfully parsed from vertical layout!")
    
    # We need to modify the top chunk to have a horizontal LinearLayout
    top_chunk = parts[0]
    # Replace the top LinearLayout orientation from vertical to horizontal
    top_chunk = top_chunk.replace('<LinearLayout\n        android:layout_width="match_parent"\n        android:layout_height="wrap_content"\n        android:orientation="vertical"', 
                                  '<LinearLayout\n        android:layout_width="match_parent"\n        android:layout_height="wrap_content"\n        android:orientation="horizontal"\n        android:baselineAligned="false"')
    
    # Also remove the Fixed Top Spacer from top_chunk as it might not be needed or we can keep it.
    # Actually, let's keep it but inside a wrapper.
    
    # Left Column: Life Total, Mana Pool
    left_col = '        <LinearLayout\n            android:layout_width="0dp"\n            android:layout_height="wrap_content"\n            android:layout_weight="1"\n            android:orientation="vertical"\n            android:layout_marginEnd="8dp">\n'
    left_col += parts[1] + parts[2] # LIFE
    left_col += parts[3] + parts[4] # MANA
    left_col += '        </LinearLayout>\n'
    
    # Right Column: Spell Counters, Creature, Planeswalker, Actions
    right_col = '        <LinearLayout\n            android:layout_width="0dp"\n            android:layout_height="wrap_content"\n            android:layout_weight="1"\n            android:orientation="vertical"\n            android:layout_marginStart="8dp">\n'
    right_col += parts[5] + parts[6] # SPELL
    right_col += parts[7] + parts[8] # CRIATURA
    right_col += parts[9] + parts[10] # PLANESWALKER
    right_col += parts[11] + parts[12] # ACTIONS
    # Remove the closing tags from part 12 to close the right_col properly
    # parts[12] ends with:
    #     </LinearLayout>
    #     </ScrollView>
    # </FrameLayout>
    
    # Let's extract the actual content of parts[12] without the closing tags.
    footer = '\n    </LinearLayout>\n    </ScrollView>\n</FrameLayout>\n'
    
    # Let's clean up part 12
    p12 = parts[12]
    # split by last </LinearLayout>
    idx = p12.rfind('</LinearLayout>')
    p12_clean = p12[:idx]
    
    right_col += p12_clean
    right_col += '        </LinearLayout>\n'
    
    new_content = top_chunk + left_col + right_col + footer

    with codecs.open(land_file_path, 'w', 'utf-8') as f:
        f.write(new_content)
    print("Landscape file successfully created.")
else:
    print("Error parsing parts. Length is:", len(parts))

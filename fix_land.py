import codecs

file_path = 'app/src/main/res/layout-land/fragment_combo.xml'
with codecs.open(file_path, 'r', 'utf-8') as f:
    lines = f.readlines()

new_lines = []
found_new_game = False

for line in lines:
    new_lines.append(line)
    
    if '@string/new_game' in line:
        found_new_game = True
        
    # the </LinearLayout> for btnNewGame is some lines after @string/new_game
    # Let's just find the `android:letterSpacing="0.1" />` that is AFTER new_game
    if found_new_game and 'android:letterSpacing="0.1" />' in line:
        new_lines.append('\n        </LinearLayout>\n    </LinearLayout>\n    </ScrollView>\n</FrameLayout>\n')
        break

with codecs.open(file_path, 'w', 'utf-8') as out:
    out.writelines(new_lines)

print('Successfully cleaned right column end')

from deep_translator import GoogleTranslator

# Ottieni il testo da tradurre come input da argomento
import sys
input_text = sys.argv[1]

# Traduci il testo
translated_text = GoogleTranslator(source='ja', target='it').translate(input_text)

# Restituisci il testo tradotto
print(translated_text)

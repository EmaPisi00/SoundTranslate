import sys
from googletrans import Translator

# Funzione che restituisce la traduzione
def traduci(testo):
    # Crea un oggetto traduttore
    translator = Translator()

    # Traduci il testo (assumiamo che sia in inglese)
    tradotto = translator.translate(testo, src='en', dest='it')

    # Restituisce solo il testo tradotto
    return tradotto.text

# Controlla se un argomento è stato passato
if len(sys.argv) > 1:
    testo = " ".join(sys.argv[1:])  # Unisci tutti gli argomenti in un'unica stringa
else:
    testo = "Default text"  # Se non c'è nessun argomento, usa un testo predefinito

# Chiamata alla funzione di traduzione
tradotto = traduci(testo)

# Restituisce il testo tradotto
print(f"{tradotto}")

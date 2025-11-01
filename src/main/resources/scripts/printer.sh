#!/bin/bash
set -e

FILE="$1"
PRINTER="$2"
UP="$3"

if [ -z "$FILE" ] || [ ! -f "$FILE" ]; then
  echo "Использование: $0 <файл> [1|2|4]"
  exit 1
fi

EXT="${FILE##*.}"
EXT_LOWER=$(echo "$EXT" | tr '[:upper:]' '[:lower:]')

TMP_PDF="/tmp/print_${RANDOM}.pdf"

case "$EXT_LOWER" in
  txt)
    enscript -B --margins=36:36:36:36 --font=Courier10 --encoding=utf-8 -o - "$FILE" | ps2pdf - "$TMP_PDF"
    ;;
  pdf)
    cp "$FILE" "$TMP_PDF"
    ;;
  doc|docx)
    libreoffice --headless --convert-to pdf --outdir /tmp "$FILE" >/dev/null 2>&1
    TMP_PDF="/tmp/$(basename "$FILE" ."$EXT").pdf"
    ;;
  jpg|jpeg|png)
    # 🧠 делаем PDF с несколькими одинаковыми страницами, чтобы number-up работал
    echo "[*] Конвертирую изображение в PDF для $UP-up печати..."
    img2pdf "$FILE" -o "$TMP_PDF"
    if [ "$UP" -gt 1 ]; then
      TMP_MULTI="/tmp/print_multi_${RANDOM}.pdf"
      # создаём PDF с тем же изображением несколько раз
      for ((i=1; i<=$UP; i++)); do
        convert "$FILE" "pdf:$TMP_MULTI" || true
      done
      # объединяем (ImageMagick создаёт по одной странице)
      pdfunite "$TMP_PDF" "$TMP_PDF" "$TMP_MULTI" "$TMP_PDF" 2>/dev/null || true
    fi
    ;;
  *)
    echo "Неподдерживаемый тип файла: $EXT_LOWER"
    exit 2
    ;;
esac

echo "[*] Печатаю ($UP страниц(ы) на листе)..."
lp -d "$PRINTER" -o number-up="$UP" "$TMP_PDF"

echo "✅ Отправлено в печать ($UP-up)"

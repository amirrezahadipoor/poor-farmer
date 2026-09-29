#!/bin/sh
set -u

status=0

report() {
  echo "comment found in $1"
  status=1
}

for file in $(find . \( -name '*.java' -o -name '*.gradle' \) -type f -not -path './.git/*' -not -path '*/build/*' -not -path './.gradle/*'); do
  if grep -nI -e '//' -e '/\*' "$file" >/dev/null 2>&1; then
    report "$file"
    grep -nI -e '//' -e '/\*' "$file" | head -5
  fi
done

for file in $(find . -name '*.xml' -type f -not -path './.git/*' -not -path '*/build/*' -not -path './.gradle/*'); do
  if grep -nI -e '<!--' -e '-->' "$file" >/dev/null 2>&1; then
    report "$file"
    grep -nI -e '<!--' -e '-->' "$file" | head -5
  fi
done

for file in $(find . -name '*.sh' -type f -not -path './.git/*' -not -path '*/build/*' -not -path './.gradle/*'); do
  if grep -nI -e ' [#]' -e '^# ' -e '^#$' "$file" >/dev/null 2>&1; then
    report "$file"
    grep -nI -e ' [#]' -e '^# ' -e '^#$' "$file" | head -5
  fi
done

if [ "$status" -ne 0 ]; then
  echo "check_no_comments: FAILED"
  exit 1
fi

echo "check_no_comments: OK"
exit 0

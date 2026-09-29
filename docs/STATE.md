# کشاورز فقیر — وضعیت ساخت

آخرین به‌روزرسانی: فاز ۰ (زیرساخت)

## آخرین مورد انجام‌شده

- [x] 0.05 ساخت `docs/STATE.md`، `docs/DESIGN.md`، `docs/BALANCE.md`

## مورد بعدی

- [ ] 0.06 ساخت Milestone و Label و Issue برای همه‌ی فازها با API

## خلاصه‌ی جلسات

### نشست ۱ (زیرساخت)

- ریپو با ROADMAP و STORY_AND_DIALOGUES به‌عنوان منبع حقیقت آماده شد.
- پروژه‌ی Gradle دو‌ماژوله‌ای (`:app` اندروید، `:core` Java خالص) با Java 17، compileSdk 36، GLSurfaceView GLES 3.0.
- `scripts/check_no_comments.sh` برای Java/XML/Gradle/Shell با تست مثبت و منفی.
- GitHub Actions: بیلد + تست واحد + lint + اسکن کامنت؛ روی `main` سبز است.
- اسناد: `docs/DESIGN.md` (تصمیم‌ها)، `docs/BALANCE.md` (اعداد اقتصادی).

## مشکلات باز

- (هیچ)

## تصمیم‌های مهم این نشست

- D01: ریپوی موجود `poor-farmer` ریپوی اصلی است (نه `keshavarz-faghir`).
- D02: نسخه‌ی قبلی ROADMAP (AMBERFIELD/Godot) با نسخه‌ی ارسالی کاربر جایگزین شد.
- D05: منطق بازی در ماژول `:core` خالص، رابط و رندر در `:app`.
- D07: compileSdk 36، AGP 8.13.2، Gradle 8.14.5.

## دستور شروع نشست بعدی

1. این فایل و `ROADMAP.md` را بخوان.
2. از اولین مورد بی‌تیک (`0.06`) شروع کن.
3. هر مورد: پیاده‌سازی، تأیید (بیلد/تست/اجرا)، تیک در ROADMAP، کامیت با قالب `<شناسه>: <شرح>`، پوش فوری.

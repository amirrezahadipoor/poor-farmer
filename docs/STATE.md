# کشاورز فقیر — وضعیت ساخت

آخرین به‌روزرسانی: پایان فاز ۰ (زیرساخت)

## آخرین مورد انجام‌شده

- [x] 0.07 ساختار پکیج‌ها و کلاس پایه‌ی `Game` با state machine صفحه‌ها

## مورد بعدی

- [ ] 1.01 حلقه‌ی بازی با timestep ثابت و رندر interpolated

## خلاصه‌ی جلسات

### نشست ۱ (زیرساخت — فاز ۰ کامل)

- ریپو با ROADMAP و STORY_AND_DIALOGUES به‌عنوان منبع حقیقت آماده شد (نسخه‌ی قبلی AMBERFIELD حذف شد، D02).
- پروژه‌ی Gradle دو‌ماژوله‌ای (`:app` اندروید، `:core` Java خالص) با Java 17، compileSdk 36، GLSurfaceView GLES 3.0.
- `scripts/check_no_comments.sh` برای Java/XML/Gradle/Shell با تست مثبت و منفی.
- GitHub Actions: بیلد + تست واحد + lint + اسکن کامنت؛ روی `main` سبز است.
- اسناد: `docs/DESIGN.md` (تصمیم‌ها D01-D15)، `docs/BALANCE.md` (اعداد اقتصادی بخش ۳).
- Milestone و Label و Issue برای ۱۳ فاز (Milestone 1-13، Issue #1-#13).
- پایه‌ی منطق در `:core`: `Game`، `GameScreen` (۲۷ صفحه)، `ScreenMachine`، `ScreenHandler`، `GameTime` (روز ۴۸۰ ثانیه‌ای، ۱۴ روز فصول، ۵۶ روز سال)، `EventBus`، `GameEvents`؛ ۲۰ تست واحد سبز.

## مشکلات باز

- (هیچ)

## تصمیم‌های مهم این نشست

- D01: ریپوی موجود `poor-farmer` ریپوی اصلی است (نه `keshavarz-faghir`).
- D02: نسخه‌ی قبلی ROADMAP (AMBERFIELD/Godot) با نسخه‌ی ارسالی کاربر جایگزین شد.
- D05: منطق بازی در ماژول `:core` خالص، رابط و رندر در `:app`.
- D07: compileSdk 36، AGP 8.13.2، Gradle 8.14.5.

## دستور شروع نشست بعدی

1. این فایل و `ROADMAP.md` را بخوان.
2. از اولین مورد بی‌تیک (`1.01`) شروع کن.
3. هر مورد: پیاده‌سازی، تأیید (بیلد/تست/اجرا)، تیک در ROADMAP، کامیت با قالب `<شناسه>: <شرح>`، پوش فوری.
4. زنجیره‌ی بیلد محلی: `JAVA_HOME=<jdk17> ./gradlew :core:test :app:build`.

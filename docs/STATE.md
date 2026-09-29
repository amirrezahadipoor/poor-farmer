# کشاورز فقیر — وضعیت ساخت

آخرین به‌روزرسانی: پایان فاز ۱ (ریاضی، مدل و بافت)

## آخرین مورد انجام‌شده

- [x] 1.06 تولید تکسچر پروسیجرال: noise، خاک، چمن، سنگ، چوب، پارچه، آب، آسمان، رنگ‌آمیزی

## مورد بعدی

- [ ] 1.07 دوربین ۲.۵بعدی با pan، pinch-zoom، چرخش ۹۰درجه‌ای و محدودیت‌ها

## خلاصه‌ی جلسات

### نشست ۱ (زیرساخت — فاز ۰ کامل)

- ریپو با ROADMAP و STORY_AND_DIALOGUES به‌عنوان منبع حقیقت آماده شد (نسخه‌ی قبلی AMBERFIELD حذف شد، D02).
- پروژه‌ی Gradle دو‌ماژوله‌ای (`:app` اندروید، `:core` Java خالص) با Java 17، compileSdk 36، GLSurfaceView GLES 3.0.
- `scripts/check_no_comments.sh` برای Java/XML/Gradle/Shell با تست مثبت و منفی.
- GitHub Actions: بیلد + تست واحد + lint + اسکن کامنت؛ روی `main` سبز است.
- اسناد: `docs/DESIGN.md` (تصمیم‌ها)، `docs/BALANCE.md` (اعداد اقتصادی بخش ۳).
- Milestone و Label و Issue برای ۱۳ فاز (Milestone 1-13، Issue #1-#13).
- پایه‌ی منطق در `:core`: `Game`، `GameScreen` (۲۷ صفحه)، `ScreenMachine`، `ScreenHandler`، `GameTime` (روز ۴۸۰ ثانیه‌ای، ۱۴ روز فصول، ۵۶ روز سال)، `EventBus`، `GameEvents`.

### نشست ۲ (فاز ۱ — 1.01 تا 1.06 کامل)

- حلقه‌ی timestep ثابت: `FixedTimestepLoop` (قدم ۱/۶۰ ثانیه، clamp ۰.۲۵، بازگرداندن alpha برای درون‌یابی) + `FixedStepConsumer`.
- کتابخانه‌ی ریاضی در `com.poorfarmer.core.math`: `Vec2/Vec3/Vec4`، `Mat4` (identity/translate/scale/rotate/lookAt/perspective/multiply/invert بر پایه‌ی minor/cofactor)، `Quat` (axis-angle، slerp، rotation matrix)؛ قرارداد float[16] ستون‌اول، rotationY(π/2): +X→(0,0,−1).
- `com.poorfarmer.core.model`: `MeshGeometry` (آرایه‌های position/normal/uv/رنگ اختیاری + indeکس، `appendVertex/appendIndex` با رشد آمورتایز، `absorb` با offset، translate/scale/rotateY، boundingBox)، `PolygonTriangulator` (ear-clipping برای outline‌های غیربرجسته)، `MeshBuilder` (box، cylinder، cone، sphere با صفح‌های قطبی دقیق، lathe با پروفایل بالا→پایین، extrude با سقف‌های triangulate‌شده، merge).
- `com.poorfarmer.core.procedural`: `Noise` (value noise، fbm، ridged، worley — همه deterministic با seed)، `Texture` (RGBA8، tint/blend/کل‌امپ)، `Ramp` (توقف‌های رنگی)، `ProceduralTextures` (خاک، چمن، سنگ، چوب، پارچه، آب، آسمان گرادیانی، ماسه).
- `com.poorfarmer.render`: `Mesh` (VAO + 3/4 VBO + индекс با GL_UNSIGNED_INT، attribute‌های 0-3: position/normal/uv/رنگ)، `TextureGL` (upload RGBA8 + mipmap + wrap)، `ShaderProgram` بازسازی شد (Builder با bindAttribute قبل از link + اعتبارسنجی GLSL پیش از کامپایل + cache یونیفرم/اتریبیوت).
- جمعاً ۹۲ تست واحد سبز در `:core` (ریاضی، mesh، بافت، زمان، صفحه‌ها، event bus، glsl validator).

## مشکلات باز

- (هیچ) — نکته: درخت ابزار (JDK/SDK) ممکن است در نشست بعدی نیاز به دانلود مجدد داشته باشد (مسیرها در «دستور شروع» ثبت است).

## تصمیم‌های مهم این نشست

- D13 تکمیل شد با پیاده‌سازی واقعی (Vec/Mat4/Quat).
- D17: کلاس‌های GL (Mesh، TextureGL، ShaderProgram) با بیلد + lint تأیید شدند؛ تأیید بصری با capture فاز ۱۱.
- D18: قرارداد هندسه/بافت ثبت شد (شمارش used جدا از capacity، winding CCW رو به بیرون، پروفایل lathe بالا→پایین، determinism بافت‌ها، امضای glGen*).

## دستور شروع نشست بعدی

1. این فایل و `ROADMAP.md` را بخوان.
2. از اولین مورد بی‌تیک (`1.07` — دوربین) شروع کن.
3. هر مورد: پیاده‌سازی، تأیید (بیلد/تست/اجرا)، تیک در ROADMAP، کامیت با قالب `<شناسه>: <شرح>`، پوش فوری.
4. زنجیره‌ی بیلد محلی: `JAVA_HOME=/home/user/.cache/tl/jdk-17.0.20.1+1 ANDROID_HOME=/home/user/.cache/android-sdk ./gradlew :core:test :app:build` (اگر جداول نبودند: Temurin 17 از api.adoptium.net، gradle-8.14.5-bin.zip، commandlinetools-linux-11076708.zip + `sdkmanager "platforms;android-36" "build-tools;36.0.0"`).
5. Issue #1 (فاز ۰) بسته شد؛ Issue هر فاز را با `Closes #N` در کامیت پایان همان فاز ببند.

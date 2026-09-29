# کشاورز فقیر — وضعیت ساخت

آخرین به‌روزرسانی: پایان کامل فاز ۱ (1.01 تا 1.20)

## آخرین مورد انجام‌شده

- [x] 1.20 پروفایلر FPS و حافظه در حالت debug

## مورد بعدی

- [ ] 2.01 زمین heightmap با noise، شیب ملایم دره‌ی کوهپایه‌ای، رنگ‌آمیزی ارتفاعی (فاز ۲ شروع می‌شود)

## خلاصه‌ی جلسات

### نشست ۱ (زیرساخت — فاز ۰ کامل)

- ریپو با ROADMAP و STORY_AND_DIALOGUES به‌عنوان منبع حقیقت آماده شد (D02).
- پروژه‌ی Gradle دو‌ماژوله‌ای (`:app` اندروید، `:core` Java خالص) با Java 17، compileSdk 36، GLSurfaceView GLES 3.0.
- `scripts/check_no_comments.sh` برای Java/XML/Gradle/Shell با تست مثبت و منفی.
- GitHub Actions: بیلد + تست واحد + lint + اسکن کامنت؛ روی `main` سبز است.
- اسناد: `docs/DESIGN.md` (تصمیم‌ها D01-D26)، `docs/BALANCE.md` (اعداد اقتصادی بخش ۳).
- Milestone و Label و Issue برای ۱۳ فاز (Milestone 1-13، Issue #1-#13).
- پایه‌ی منطق در `:core`: `Game`، `GameScreen` (۲۷ صفحه)، `ScreenMachine`، `ScreenHandler`، `GameTime`، `EventBus`، `GameEvents`.

### نشست ۲ (فاز ۱ تا 1.11)

- حلقه‌ی timestep ثابت: `FixedTimestepLoop` (قدم ۱/۶۰ ثانیه، clamp ۰.۲۵، alpha درون‌یابی).
- کتابخانه‌ی ریاضی: `Vec2/Vec3/Vec4`، `Mat4`، `Quat`؛ قرارداد float[16] ستون‌اول.
- `com.poorfarmer.core.model`: `MeshGeometry`، `PolygonTriangulator` (ear-clipping)، `MeshBuilder`.
- `com.poorfarmer.core.procedural`: `Noise` (value/fbm/ridged/worley با seed)، `Texture`، `Ramp`، `ProceduralTextures`.
- `com.poorfarmer.render`: `Mesh`، `TextureGL`، `ShaderProgram` (Builder + اعتبارسنجی GLSL).

### نشست ۳ (فاز ۱ تکمیل — 1.12 تا 1.20)

- **1.12** سیستم ذره با استخر و پیش‌تنظیم dust/water/rain/snow/leaf/spark/coin/smoke (`core/world/ParticleSystem` + `ParticleRenderer` GL_POINTS).
- **1.13** سلسله‌مراتب اسکلت و انیمیشن کدی با blend (`core/entity`: Bone/Skeleton/Pose/AnimationClip/AnimationMixer — حداکثر ۴ صدا، کراس‌فید، بدون allocation در update).
- **1.14** پست‌پردازش: vignette + رنگ‌بندی فصلی + bloom سبک غروب (`PostProcessParams` خالص + `PostProcessor` FBO با extract/blur/composite؛ LOW بلوم را حذف می‌کند).
- **1.15** ورودی لمسی: tap/long-press/drag/pinch + hit-test شبکه و موجودیت‌ها (`core/ui/TouchInput` خالص + `HitTester`/`HitTarget` + `GameEvents.Tapped/LongPressed`).
- **1.16** event bus و زمان با سرعت ۱x/۲x: `SpeedChanged` روی تغییر واقعی منتشر می‌شود.
- **1.17** `ObjectPool` + ممنوعیت allocation در حلقه با تست heap (`AllocationProbe`)؛ نشت واقعی پیدا و اصلاح شد (multiply ایلیز Mat4 → ThreadLocal موقت).
- **1.18** `JobQueue` (worker daemon، poll از رشته‌ی بازی) + `LoadingProgress` + صفحه‌ی لودینگ RTL با ارقام فارسی؛ بارگذاری واقعی بوت: field ارتفاع + مش Terrain + ۳ بافت → `BootAssets`.
- **1.19** پایه‌ی JSON خالص (`core/json/Json`) + `SessionState` versioned + `AtomicFileStore` (tmp+rename)؛ restore در onCreate و autosave در onPause/onDestroy/DayChanged؛ بازیابی از context-loss در PostProcessor.
- **1.20** `FpsMeter` + `MemoryProbe`/`JvmMemoryProbe` + `DebugHudView` (فقط BuildConfig.DEBUG؛ `buildFeatures.buildConfig true` اضافه شد).
- جمعاً **۲۴۷ تست واحد سبز** در `:core`.
- Issue #1 (فاز ۰) و Issue #2 (فاز ۱) بسته شده‌اند.

## مشکلات باز

- (هیچ) — نکته: درخت ابزار (JDK/SDK) ممکن است در نشست بعدی نیاز به دانلود مجدد داشته باشد (مسیرها در «دستور شروع» ثبت است).
- فایل `Terrain.java` (متعلق به 2.01) از قبل وجود دارد و کامپایل می‌شود (به‌صورت تصادفی در کامیت 1.12 رفت)؛ تیک 2.01 هنوز نزده است و تست/وایرینگ آن در نوبت خودش انجام می‌شود.

## تصمیم‌های مهم این نشست

- D21: معماری پست‌پردازش و پارامترهای فصلی/غروب.
- D22: ژست‌ها در `:core` با آستانه‌های ثبت‌شده و hit-test با `HitTarget`.
- D23: `ObjectPool` + اعتبارسنجی ممنوعیت allocation با تست heap.
- D24: `JobQueue` بدون Handler/Looper و صفحه‌ی لودینگ با کار واقعی بوت.
- D25: JSON خالص + session versioned + نوشتن اتمیک؛ continue-after-kill = زمان و سرعت.
- D26: پروفایلر فقط در variant debug.

## دستور شروع نشست بعدی

1. این فایل و `ROADMAP.md` را بخوان.
2. از اولین مورد بی‌تیک (`2.01`) شروع کن — دقیقاً به ترتیب ROADMAP.
3. هر مورد: پیاده‌سازی، تأیید (بیلد/تست/اجرا)، تیک در ROADMAP (فقط با python، هرگز نه sed)، کامیت با قالب `<شناسه>: <شرح>` + فایل‌های مشخص (هرگز `git add -A`)، پوش فوری.
4. زنجیره‌ی بیلد محلی: `JAVA_HOME=/home/user/.cache/tl/jdk-17.0.20.1+1 ANDROID_HOME=/home/user/.cache/android-sdk ./gradlew :core:test :app:build` (اگر جداول نبودند: Temurin 17 از api.adoptium.net، gradle-8.14.5-bin.zip، commandlinetools-linux-11076708.zip + `sdkmanager "platforms;android-36" "build-tools;36.0.0"`).
5. Issue هر فاز را با `Closes #N` در کامیت پایان همان فاز ببند (Issue #3 = فاز ۲).

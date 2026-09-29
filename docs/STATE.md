# کشاورز فقیر — وضعیت ساخت

آخرین به‌روزرسانی: پایان آیتم 2.06 (فاز ۲ تا 2.06 کامل)

## آخرین مورد انجام‌شده

- [x] 2.06 انبار، طویله، مرغداری، کندو، تنور، آسیاب، کارگاه قالی، دیگ گلاب، خشک‌کن زعفران، گلخانه، سردخانه

## مورد بعدی

- [ ] 2.07 روستا: مغازه‌ی بتول، تعاونی، تعمیرگاه جلال، مسجد، مدرسه، چایخانه، خانه‌های همسایه‌ها

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

- حلقه‌ی timestep ثابت: `FixedTimestepLoop` (قدم ۱/۶۰ ثانیه، clamp ۰٫۲۵، alpha درون‌یابی).
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
- Issue #1 (فاز ۰) و Issue #2 (فاز ۱) بسته شده‌اند.

### نشست ۴ (فاز ۲: 2.01 تا 2.06)

- **2.01** زمین heightmap با fbm/ridge در `core/world/Terrain` (دنیای ۲۴۱×۲۴۱، دایره‌ی تخت ۳۶m با ارتفاع ۱٫۲m، رنگ‌های ارتفاعی + شیب)؛ تست و وایرینگ (D27).
- **2.02** مزرعه‌ی ۴۸×۴۸: `FarmGrid` (cell ۱m، stateVersion) + `FarmRenderer` overlay با بافت‌های بافتی + سلول فعال (D28).
- **2.03** رودخانه: `River` (نوار fbm در امتداد x، عمق‌دهی بستر + مش روبان + ذره‌ی آب) و رفع ناهماهنگی loading ۵/۴ (D29).
- **2.04** آسمان تولیدی: `SkyPalette` (موقعیت خورشید/ماه، کف دودکی، ستاره‌ها)، `BackgroundMountains` (حلقه‌ی کوه دوردست)، `SkyRenderer` (مثلث تمام‌صفحه + خورشید/ماه/ستاره + مه زیر افق)، `CloudRenderer`/`CloudLayer` (ابر fbm شناور، در LOW خاموش) (D30).
- **2.05** خانه‌ی پدربزرگ: `GrandfatherHouse` با سه مرحله (ویران ۱۴۴ مثلث / تعمیرشده ۱۷۲ / کامل ۲۶۸) + داخل خانه‌ی مبلمان‌دار ۳۱۶ مثلث؛ version-based dirty-check در SceneRenderer؛ flag داخل‌خانه‌ای (ورود واقعی در فاز ۴)؛ رفع دو باگ مدل (رشد رنگ در `setVertexColor` و کپی extra در `absorb`) با تست رگرسیون (D31).
- **2.06** یازده آبنمای مزرعه: `FarmBuildings` با `BuildingKind` ۱۱ عضوی، موقعیت‌های ثابت روی نوار تخت (شعاع ۲۴–۳۵٫۵m)، ۸۸۸ مثلث کل در یک مش ترکیبی؛ تنور گنبدی lathe، آسیاب با پره، گلخانه شیشه‌ای، دیگ گلاب استوانه‌ای؛ رفع باگ بوت واقعی: `LoadingProgress` total ۵ در برابر ۴ واحد کامل → صفحه‌ی لودینگ روی ۸۰٪ می‌ماند (D32).
- جمعاً **۳۰۶ تست واحد سبز** در `:core`.
- Issue #14 (آیتم بزرگ 2.05) با کامیت همان آیتم بسته شد. Issue #3 (فاز ۲) با کامیت آیتم پایانی فاز (2.16) بسته می‌شود.

## مشکلات باز

- (هیچ) — نکته: درخت ابزار (JDK/SDK) ممکن است در نشست بعدی نیاز به دانلود مجدد داشته باشد (مسیرها در «دستور شروع» ثبت است).

## تصمیم‌های مهم این نشست

- D27: زمین fbm + دایره‌ی تخت مزرعه و رنگ‌های ارتفاعی.
- D28: مزرعه به‌صورت overlay روی زمین؛ stateVersion برای dirty-check.
- D29: رودخانه‌ی نوار fbm + بستر فرورفته و روبان آب.
- D30: آسمان/کوه/ابر تولیدی و `SkyPalette` مشترک.
- D31: خانه‌ی پدربزرگ سه‌مرحله‌ای + داخل خانه؛ کشش مش در خود آبجکت و آپلود مجدد فقط هنگام تغییر version.
- D32: یازده آبنمای مزرعه در یک مش ترکیبی + موقعیت‌های ثابت برای hit-test/واکاوی بعدی؛ رفع باگ total/units صفحه‌ی لودینگ.

## دستور شروع نشست بعدی

1. این فایل و `ROADMAP.md` را بخوان.
2. از اولین مورد بی‌تیک (`2.07`) شروع کن — دقیقاً به ترتیب ROADMAP.
3. هر مورد: پیاده‌سازی، تأیید (بیلد/تست/اجرا)، تیک در ROADMAP (فقط با python، هرگز نه sed)، کامیت با قالب `<شناسه>: <شرح>` + فایل‌های مشخص (هرگز `git add -A`)، پوش فوری.
4. زنجیره‌ی بیلد محلی: `JAVA_HOME=/home/user/.cache/tl/jdk-17.0.20.1+1 ANDROID_HOME=/home/user/.cache/android-sdk ./gradlew :core:test :app:build` (اگر جداول نبودند: Temurin 17 از api.adoptium.net، gradle-8.14.5-bin.zip، commandlinetools-linux-11076708.zip + `sdkmanager "platforms;android-36" "build-tools;36.0.0"`).
5. Issue هر فاز را با `Closes #N` در کامیت پایان همان فاز ببند (Issue #3 = فاز ۲؛ آیتم‌های بزرگ می‌توانند Issue جداگانه بگیرند مثل #14 برای 2.05).

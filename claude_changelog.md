# Claude Changelog

## 2026-06-23 — Editorial UI redesign + real EXIF metadata

Aligned the gallery UI with `new-wireframe-prototype/`:

**Backend**
- Added `com.drewnoakes:metadata-extractor` (2.19.0) to `pom.xml`.
- `model/Photo.java`: added `title`, `dateTaken`, `width`, `height`, `exif` fields.
- New `model/ExifEntry.java` record (`label`, `value`).
- New `service/MetadataService.java`: extracts title (prettified filename), date taken,
  pixel dimensions, and EXIF rows (Camera, Lens, Focal, Aperture, Shutter, ISO) via
  metadata-extractor. Results cached in-memory by filename+mtime. Gracefully handles
  EXIF-less / non-image files.
- `service/PhotoService.java`: enriches each Photo via MetadataService in `getPhotos`/`getPhoto`.
- Updated `PhotoServiceTest` constructor call for the new dependency.

**Frontend**
- `templates/index.html`: Google Fonts (Fraunces/Inter/JetBrains Mono), header eyebrow +
  serif title + pill theme toggle, split modal (image + EXIF side panel).
- `static/css/styles.css`: rewritten to warm palette, CSS-columns masonry preserving native
  aspect ratios, hover-caption tiles, animated split modal, circular blurred close button.
- `static/js/gallery.js`: figure/figcaption tiles with title + aspect-ratio; pill toggle
  icon/label; modal populates date + EXIF table; modal uses `.open` class; history state
  carries the full photo object; backdrop click-to-close.

Verified: `./mvnw test` (26 pass); ran the app — `/api/photos` returns real EXIF
(Canon EOS R8, lens, f-stop, shutter, ISO, dimensions), index/CSS/thumbnail endpoints all 200.

## 2026-06-24 — Fix panorama squish + scroll shuffle

- **Scroll shuffle:** `PhotoService.listPhotoFiles()` sorted by mtime with the comparator
  returning 0 for equal timestamps — unstable over `Files.list`, so pages reordered between
  requests and photos appeared to shuffle/duplicate while scrolling. Added a filename
  tiebreaker for a deterministic order.
- **Panorama squish:** ultra-wide images (e.g. 9:1) were crushed into 33px-tall slivers in a
  single masonry column. `gallery.js` now tags images with ratio > 2.4 as `.panorama`, and
  `styles.css` gives them `column-span: all` so they render as full-width bands.

Verified with Playwright against the JetBrains-launched app: no pixel distortion (rendered
ratio matches natural ratio); 216 scrolled tiles all unique (shuffle gone); 9:1 panorama now
1264x138 full-width; modal still shows image + 6 EXIF rows + date.

## 2026-06-24 — Stop tiles realigning on infinite scroll

The masonry used CSS `columns`, which rebalances **every** item across columns each time new
tiles are appended — already-rendered photos visibly jumped/realigned while scrolling. Replaced
it with a CSS-grid masonry:

- `styles.css`: `.photo-carousel` is now `display: grid` with `grid-template-columns:
  repeat(auto-fill, minmax(280px, 1fr))` and `grid-auto-rows: 8px`. Panoramas use
  `grid-column: 1 / -1` (was `column-span: all`). Mobile query updated to grid too.
- `gallery.js`: each tile gets `grid-row-end: span N` computed from its laid-out height
  (`setSpan`), with an `img.load` fallback for photos lacking known dimensions, and a debounced
  `resize` handler that recomputes all spans. Grid places items in source order and never
  repositions earlier tiles on append.

Verified with Playwright: after 8 scroll passes (216 tiles, 0 duplicates) the first 12 tiles
had identical position/order before vs after (0 moved); panoramas still full-width (1264px).

## 2026-06-23 — Dimension-aware panorama thumbnails + modal full-size link

- **Sharper panoramas:** `ThumbnailService.createThumbnail` now reads the source image's header
  dimensions (cheap, no full decode) and, for very wide images (aspect ratio > 2.4, matching the
  frontend's panorama threshold), scales the thumbnail box 4× — panoramas render full-gallery-width
  on the page, so a 300/400px-wide thumbnail looked blurry. Cache keys are the source SHA, so
  `cache/` was cleared to regenerate. Verified: a panorama thumbnail is now 1600×428 vs 400px-wide
  normal tiles.
- **Open full size in a new tab:** added a `View full size ↗` link to the modal info panel
  (`index.html` + styled in `styles.css`), wired in `gallery.js`'s `openModal` to point at the
  photo's `/full` URL (`target="_blank" rel="noopener"`). Verified via Playwright.

App restarted via the JetBrains `PhotosApplication` run config to pick up the new template + class.

## 2026-06-24 — Fix CI Java version

CI was installing JDK 24 while the project targets Java 25, causing `release version 25 not supported`. Bumped setup-java in `.github/workflows/build.yml` to 25.

## 2026-06-24 — Fix OOM: honor JAVA_OPTS in Docker

- `Dockerfile`: changed the exec-form ENTRYPOINT to `sh -c "exec java $JAVA_OPTS -jar /app/app.jar"`.
  The old form did no shell expansion, so the `-Xmx`/`MaxRAMPercentage` settings in
  docker-compose were silently ignored — the JVM ran on ~25% of the 512M limit (~128MB heap),
  too little to decode full-res JPEGs into BufferedImages and OOMed during thumbnail pre-gen.

# Natlog 15. nov. 2026 – V2.0 og V3.0

Skrevet af Claude Code undervejs i natten. Tider er lokal tid (CET).

## Forløb (skrives løbende)

### Trin 0 – slice 3 kompilerer
- Start 01:05. Begge repos på `v2-v3-night` (tjekket med `git branch --show-current`).
- `./mvnw -q compile`: ingen fejl. Ingen rettelser, derfor intet rettelses-commit.
- Appen startet med `./mvnw spring-boot:run`, `GET /api/crosswords` svarede 200 med de gemte krydsord. Appen stoppet.
- Miljøvariabler: kun tjekket at navnene er sat (ja/nej), værdier ikke læst. `night.env` er ikke åbnet og ikke committet.
- Kort snak med Erik kl. ca. 01:08: integrationen med texs og Anthropic skal ikke testes i nat (svarformaterne kendes).
- Slut 01:10.

### Slice 4 – brugere
- Start 01:10.
- `spring-boot-starter-security` og `spring-security-test` (test-scope) tilføjet til pom.xml. Versioner styres af Spring Boot-parent.
- Nyt: `AppUserRepository` (findByUsername, existsByUsername), `SecurityConfig` (PasswordEncoder-bean med BCrypt
  + midlertidig minimal SecurityFilterChain, se beslutning 49), `AppUserDetailsService`, `UserService`,
  `UserController`, `UserRegistrationDto`, `UserDto`, `UsernameTakenException` (409), `InvalidRegistrationException` (400).
- `ControllerExceptionHandler`: nye handlers for 409/400, og `HttpMessageNotReadableException` → 400
  (manglende/ødelagt JSON-body gav ellers 500).
- Kompilering: ingen fejl i første forsøg.
- Kørt med curl (app startet og stoppet igen):
  - register → 201 `{"id":1,"username":"natbruger",...,"role":"PLAYER"}` uden hash
  - samme brugernavn igen → 409, blankt brugernavn → 400, manglende felter → 400, tom body → 400, password på 80 tegn → 400
  - `/me` med credentials → 200, uden → 401, forkert password → 401
- Hvor jeg gik i stå (ca. 2 min.): de første curl-kald gav 500. Årsagen var min test, ikke koden: i zsh splittes en
  variabel som `$J="-H Content-Type:..."` ikke i ord, så headeren blev sendt forkert. Med rigtig quoting virkede alt.
  Sidegevinst: en forkert Content-Type giver 500 i stedet for 415, fordi `everythingElse` fanger
  `HttpMediaTypeNotSupportedException`. Ikke rettet (findes også i V1, ikke en del af briefen) – noteret til Erik.
- Testbrugeren `natbruger` ligger nu i MySQL-tabellen `app_user` (der må ikke slettes i nat).
- Slut 01:13.

### Slice 5 – adgang
- Start 01:13.
- `SecurityConfig`: HTTP Basic, `SessionCreationPolicy.STATELESS`, CSRF slået fra (begrundet i kommentar), CORS via
  `CorsConfigurationSource`-bean for `http://localhost:63342` (GET, POST, OPTIONS; headers Authorization, Content-Type).
  Register og `/error` er åbne, alt andet kræver login.
- `@CrossOrigin` fjernet fra alle tre controllere (beslutning 53).
- `JsonAuthenticationEntryPoint`: 401 som JSON i `ErrorMessage`-formen, uden `WWW-Authenticate`. Bruges både af
  `httpBasic` (forkert password) og `exceptionHandling` (ingen credentials).
- Bruger sat på nye krydsord og afleveringer: controllerne tager `Principal`, services slår `AppUser` op via
  `UserService.findEntityByUsername`. Claude-afleveringer får den bruger, der bad Claude løse (beslutning 54).
  `SubmissionDto` har fået `username` (null for gamle afleveringer). `Crossword`-konstruktøren har fået `user`.
- Kompilering: ingen fejl.
- Kørt med curl:
  - `GET /api/crosswords` uden login → 401 JSON, ingen `WWW-Authenticate`, ingen `Set-Cookie`; med login → 200
  - `POST /api/crosswords` uden login → 401 (med login ikke kørt – det ville kalde texs)
  - `POST .../submissions/claude` uden login → 401 (med login ikke kørt – det ville kalde Anthropic)
  - forkert password → 401 JSON
  - preflight fra 63342 → 200 med `Access-Control-Allow-Origin/Methods/Headers`; fra fremmed origin → 403
  - spiller-aflevering med login → 201 med `"username":"natbruger"`; listen viser gamle afleveringer med `username: null`
  - register stadig åben → 201 (testbruger `natbruger2` oprettet)
- Fundet, ikke rettet: en ukendt sti med login (`/api/nothing`) giver 500 i stedet for 404, fordi `everythingElse`
  fanger `NoResourceFoundException`. Samme opførsel som før V2.0. Noteret til Erik.
- Slut 01:15.

### Slice 6 – login i frontend
- Start 01:15.
- Nyt i frontend: `js/auth.js` (credentials i en modul-variabel, beslutning 55), `js/components/authForms.js`
  (login- og registreringsformular). `crosswordApi.js`: `request()` sender `Authorization: Basic ...`, glemmer
  credentials og kalder en 401-handler ved 401; nye funktioner `registerUser` og `fetchMe`.
- `index.js`: siden starter med login. Login tjekkes med `GET /api/users/me`. Registrering logger ind bagefter.
  "Log ud" glemmer credentials. Enhver 401 viser login igen.
- Basic-headeren bygges fra UTF-8-bytes, fordi `btoa` ikke tåler æ/ø/å (beslutning 56).
- CSS: `[hidden] { display: none !important; }`, fordi `display: flex` ellers overstyrer `hidden`-attributten
  (fanget ved gennemlæsning, ikke i en browser).
- Afleveringslisten viser brugernavn (og "startet af" for Claude).
- Kørt: `node --check` på alle JS-filer (ok). Frontend er IKKE testet i en browser.
- Kørt med curl mod backend: register (med æøå i password) → 201, `/me` med UTF-8-Basic → 200, uden → 401,
  forkert password → 401, OPTIONS-preflight → CORS-headere, `POST /api/crosswords` uden login → 401.
- Ingen kald til texs eller Anthropic.
- Slut 01:17.

### Slice 7 – gitterstørrelser
- Start 01:17.
- Nye templates: `small-7x7`, `medium-9x9`, `large-13x13` (mini-5x5 uændret, id 1).
  - 7×7 er lavet i hånden. 9×9 og 13×13 er fundet med et lille Python-søgescript (hill-climbing over symmetriske
    par af sorte felter). Ingen ord er længere end 7 bogstaver for at gøre det lettere for texs (beslutning 57).
  - Kontrolleret to gange: et Python-tjek (180°-symmetri, ingen løb på 2, hver hvid celle i et ord ≥ 3, forbundet)
    og den rigtige `GridWordFinder` kørt via `jshell --class-path target/classes`:
    7×7: 22 ord, 3–7 bogstaver, 41/41 hvide celler dækket. 9×9: 30 ord, 3–6, 62/62. 13×13: 58 ord, 3–6, 135/135.
  - Krav om "ingen løb på 2" gælder kun de nye: mini-5x5 har løb på 2 i række 0 og 4 (ingen ord der, dækket lodret).
    Ikke ændret, fordi gemte krydsord peger på den.
- `InitData` tilføjer manglende templates efter navn (`existsByName`) og rører ikke eksisterende.
  Kørt: 1. start indsatte 3 rækker, 2. start indsatte 0.
- `GET /api/grid-templates` → `[{id,name,rows,cols}]` (ny `GridTemplateService`, `GridTemplateController`, `GridTemplateDto`).
- `POST /api/crosswords` tager `{gridTemplateId, playerPrompt}` (`CreateCrosswordRequestDto`). Templaten slås op før
  texs-kaldet, så et ukendt id giver 404 uden at bruge kvote. Tom playerPrompt gemmes som null.
  `findFirstByOrderByIdAsc` fjernet (ikke længere brugt).
- Kørt med curl: templates → 200 med 4 rækker, uden login → 401, ukendt id → 404, ingen body → 400, gammelt krydsord → 200.
  IKKE kørt: oprettelse med gyldigt id (ville kalde texs).
- **De nye templates er ikke testet mod texs** (dummy-nøgle). texs kan fejle på store gitre inden for `max_time` = 20 s
  (`CrosswordApiClient.MAX_SECONDS`), især 13×13. Fejler fill, svarer backenden 502 med texs' `reason`.
- Midlertidigt brud: frontendens "Hent nyt krydsord" sender ingen body og får 400, indtil slice 9 er committet.
- Slut 01:21.

### Slice 8 – tema i fritekst
- Start 01:21.
- Læst https://crossword.texs.org/crossword-api (kun dokumentationssiden, intet API-kald). Resultat:
  - `POST /fill` tager kun `grid`, `min_score`, `max_time` (1–30, standard 25), `language`, `try_hard_grid`.
    **Ingen parameter til tema eller ord, der skal med.** Man kan kun påvirke fyldet med faste bogstaver i gitteret.
  - `POST /fill/build` har `theme_words`, men kun for koreansk, kinesisk og japansk. `POST /fill/improve` har `locked`
    til at beskytte tema-celler, men kræver, at nogen først har valgt tema-ordene.
- Derfor: playerPrompt gemmes som krydsordets tema og vises (`CrosswordDto.theme`). **Brugerhistorien kan kun delvist
  opfyldes:** temaet påvirker ikke ordene. Claude genererer ikke ord eller gitter (beslutning 58).
- Tema over 255 tegn → 400 (`InvalidCrosswordRequestException`), tjekket før texs-kaldet (kolonnen er VARCHAR(255)).
- Kørt med curl: tema på 300 tegn → 400 uden texs-kald; `GET /api/crosswords/1` viser `"theme":null` for gamle krydsord.
- Slut 01:22.

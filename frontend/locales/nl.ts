import { LocaleMessageObject } from 'vue-i18n';
const msgs: LocaleMessageObject = {
  general: {
    close: "Sluiten",
    submit: "Indienen",
    save: "Opslaan",
    comment: "Reageer",
    change: "Verander",
    donate: "Doneer",
    continue: "Verdergaan",
    create: "Creëer",
    delete: "Verwijder",
    or: "Of",
    reset: 'Reset',
    edit: "Bewerk",
    required: "Vereist",
    add: "Voeg toe",
    name: "Naam",
    link: "Koppeling",
    send: "Stuur",
    home: "Startpagina",
    message: "Bericht",
    refresh: "Ververs",
    confirm: "Bevestigen",
    error: {
      invalidUrl: "Ongeldig URL-formaat"
    }
  },
  hangar: {
    projectSearch: {
      query: "Zoek tussen {0} projects, trots gemaakt door de community...",
      relevanceSort: "Sorteer op relevantie",
      noProjects: "Er zijn geen projecten. 😢",
      noProjectsFound: "0 projecten gevonden. 😢"
    },
    subtitle: "Een Minecraft package repository",
    sponsoredBy: "Gesponsord door"
  },
  pages: {
    staffTitle: "Personeel",
    authorsTitle: "Auteurs",
    headers: {
      username: "Gebruikersnaam",
      roles: "Rollen",
      joined: "Aangesloten",
      projects: "Projecten"
    }
  },
  nav: {
    login: "Log in",
    signup: "Inschrijven",
    user: {
      notifications: "Berichten",
      flags: "Vlaggen",
      projectApprovals: "Projectgoedkeuringen",
      versionApprovals: "Versiegoedkeuringen",
      stats: "Statistieken",
      health: "Hangar Gezondheid",
      log: "Gebruikersactie Log",
      platformVersions: "Platformversies",
      logout: "Log uit",
      error: {
        loginFailed: "Authenticatie Mislukt",
        invalidUsername: "Ongeldige gebruikersnaam",
        hangarAuth: "Kon niet verbinden met HangarAuth",
        loginDisabled: "Login is tijdelijk niet beschikbaar, probeer het later opnieuw",
        fakeUserEnabled: "Nepgebruiker is aangezet. {0} is dus uit"
      }
    },
    createNew: "Maak nieuw...",
    new: {
      project: "Nieuw Project",
      organization: "Nieuwe Organisatie"
    },
    hangar: {
      home: "Homepagina",
      forums: 'Forums',
      code: 'Code',
      docs: "Documentatie",
      javadocs: 'JavaDocs',
      hangar: 'Hangar (Plugins)',
      downloads: "downloads",
      community: "Gemeenschap",
      paper: 'PaperMC'
    }
  },
  project: {
    stargazers: "Sterrenkijkers",
    noStargazers: "Er zijn nog geen sterrenkijkers voor dit project 😢",
    watchers: "Volgers",
    noWatchers: "Er zijn nog geen volgers van dit project 😢",
    members: "Leden",
    category: {
      info: "Categorie: {0}",
      admin_tools: "Administratortools",
      chat: "Chatten",
      dev_tools: "Onwikkelaarstools",
      economy: "Economie",
      gameplay: "Spel",
      games: "Spellen",
      protection: "Bescherming",
      role_playing: "Rolspelen",
      world_management: "Wereldbeheer",
      misc: "Verschillende"
    },
    actions: {
      unwatch: "Ontvolg",
      watch: "Volg",
      flag: "Vlag",
      star: "Ster",
      unstar: "Ontster",
      adminActions: "Administratoracties",
      flagHistory: "Vlaggeschiedenis ({0})",
      staffNotes: "Personeelsnotities ({0})",
      userActionLogs: "Gebruikersctielogs",
      forum: 'Forum'
    },
    flag: {
      flagProject: "Vlag {0}?",
      flagSend: "Succesvol gevlagd, dank je dat je de community een betere plek maakt!",
      flagSent: "Melding ingediend voor beoordeling",
      flags: {
        inappropriateContent: "Ongepaste Content",
        impersonation: "Impersonatie of Bedrog",
        spam: 'Spam',
        malIntent: "Slechte Intenties",
        other: "Anders"
      },
      error: {
        alreadyOpen: "Je kan maar 1 onopgeloste vlag op een project hebben",
        alreadyResolved: "Deze vlag is al opgelost"
      }
    },
    tabs: {
      docs: "Documentatie",
      versions: "Versies",
      discuss: "Discussieer",
      settings: "Instelling",
      homepage: "Homepagina",
      issues: "Problemen",
      source: "Broncode",
      support: "Ondersteuning"
    },
    new: {
      step1: {
        title: "Gebruikersovereenkomst",
        text: "Een project bevat de documentatie en de downloads van je plugin.<br>Voordat je verder gaat, lees alsjeblieft de <a href=\"#\">Hangar Indieningsrichlijnen.</a>",
        continue: "Akkoord",
        back: "Stoppen"
      },
      step2: {
        title: "Basisinstellingen",
        continue: "Verdergaan",
        back: "Terug",
        userSelect: "Creëer als...",
        projectName: "Projectnaam",
        projectSummary: "Projectsamenvatting",
        projectCategory: "Projectcategorie"
      },
      step3: {
        title: "Extra instellingen",
        continue: "Verdergaan",
        back: "Terug",
        optional: "Optioneel",
        links: "Koppelingen",
        homepage: "Homepagina",
        issues: "Probleemtracker",
        source: "Broncode",
        support: "Externe Ondersteuning",
        license: "Licentie",
        type: 'Type',
        customName: "Naam",
        url: 'URL',
        seo: 'SEO',
        keywords: "Sleutelwoorden"
      },
      step4: {
        title: "Importeer van Spigot",
        continue: "Verdergaan",
        back: "Terug",
        optional: "Optioneel",
        convert: "Converteer",
        saveAsHomePage: "Opslaan als Homepagina",
        convertLabels: {
          bbCode: "Plak je BBCode hier",
          output: "Markdown Uitvoer"
        },
        preview: "Voorvertoning",
        tutorial: "Hoe je de BBCode krijgt",
        tutorialInstructions: {
          line1: "Om de BBCode van je Spigot resource te halen, doe je het volgende:",
          line2: "1. Ga naar je project en klik op \"Edit Resource\".",
          line3: "2. Klik op het sleutelsymbol in de beschrijvingseditor.",
          line4: "3. Kopiëer-plak de inhoud in de bovenste converteertextbox, maak wat veranderingen waar nodig, en klik op opslaan!"
        }
      },
      step5: {
        title: "Afwerken",
        text: "Creëren..."
      },
      error: {
        create: "Er is een fout opgetreden tijdens het aanmaken van het project",
        nameExists: "Er bestaat al een project met deze naam",
        slugExists: "Er bestaat al een project met deze slug",
        invalidName: "De naam bevat ongeldige tekens",
        tooLongName: "Projectnaam is te lang",
        tooLongDesc: "Projectbeschrijving is te lang",
        tooManyKeywords: "Project heeft teveel keywords",
        noCategory: "Project moet een categorie hebben",
        noDescription: "Project moet een beschrijving hebben"
      }
    },
    sendForApproval: "Sturen voor goedkeuring",
    info: {
      title: "Informatie",
      publishDate: "Gepubliceerd op {0}",
      views: "0 weergaven | {0} weergave | {0} weergaven",
      totalDownloads: "0 totale downloads | {0} totale download | {0} totale downloads",
      stars: "0 sterren | {0} ster | {0} sterren",
      watchers: "0 volgers | {0} volger | {0} volgers"
    },
    promotedVersions: "Gepromote versies",
    license: {
      link: "Gelicenseerd onder "
    },
    error: {
      star: "Kon sterren niet omzetten",
      watch: "Kon volgen niet omzetten"
    },
    settings: {
      title: "Instellingen",
      category: "Categorie",
      categorySub: "Categoriseer je project in 1 van 10 categories. Je project in de juiste categorie plaatsen maakt het makkelijker voor andere mensen het te vinden.",
      keywords: "Sleutelwoorden",
      keywordsSub: "Dit zijn speciale woorden die jouw project moeten laten zien als mensen ernaar zoeken.",
      homepage: "Homepagina",
      homepageSub: "Een eigen homepagina voor je project laat het er professioneler, officiëler, een geeft je nog een plek om informatie over je project te krijgen.",
      issues: "Probleemtracker",
      issuesSub: "Een probleemtracker toevoegen geeft je gebruikers een makkelijke manier om ondersteuning te krijgen en maakt het makkelijk om bugs bij te houden.",
      source: "Broncode",
      sourceSub: "Ondersteun de gemeenschap van ontwikkelaars door je project open source te maken!",
      support: "Externe ondersteuning",
      supportSub: "Een externe plaats waar je ondersteuning kan geven. Dit zou een forum, een Discordserver , of iets anders kunnen zijn.",
      license: "Licentie",
      licenseSub: "Wat kunnen mensen wel (en niet) doen met jouw project?",
      forum: "Maak posts op het forum",
      forumSub: "Zorgt ervoor of dingen zoals een nieuwe update een forumpost maakt.",
      description: "Beschrijving",
      descriptionSub: "Een korte beschrijving van je project",
      icon: "Icoon",
      iconSub: "Upload het icoon van je project.",
      iconUpload: "Uploaden",
      iconReset: "Reset Icoon",
      apiKey: "API sleutels",
      apiKeySub: "Genereer een unieke deployment key om build deployment van Gradle aan te zetten",
      apiKeyGenerate: "Genereer",
      rename: "Hernoem",
      renameSub: "Het veranderen van de naam van je project kan ongewenste gevolgen met zich meebrengen. We zetten geen herdirecties op.",
      delete: "Verwijderen",
      deleteSub: "Als je je project verwijdert, kan het niet meer hersteld worden.",
      hardDelete: "Hard verwijderen",
      hardDeleteSub: "Als je je project verwijdert, kan het niet meer hersteld worden. Dit keer voor het echie...",
      save: "Wijzigingen opslaan",
      optional: "(optioneel)",
      licenseCustom: "Eigennaam",
      licenseType: 'Type',
      licenseUrl: 'URL',
      donation: {
        enable: "Aanzetten",
        enableSub: "Zet het donatieformulier aan",
        email: "E-mail",
        emailSub: "Het e-mailadres van het paypal account dat de donaties moet ontvangen",
        defaultAmount: "Standaardhoeveelheid",
        defaultAmountSub: "De vooringestelde standaardhoeveelheid",
        oneTimeAmounts: "Eenmaalhoeveelheden",
        oneTimeAmountsSub: "Lijst van de opties die je gebruikers wil geven voor eenmalige donaties. Gebruikers kunnen altijd zelf een hoeveelheid kiezen",
        monthlyAmounts: "Maandelijkse hoeveelheden",
        monthlyAmountsSub: "Lijst van de opties die je gebruikers wil geven voor maandelijkse donaties. Gebruikers kunnen altijd zelf een hoeveelheid kiezen"
      },
      error: {
        invalidFile: "{0} is een ongeldig bestandstype",
        noFile: "Geen bestand ingediend",
        members: {
          invalidUser: "{0} is geen geldige gebruiker",
          alreadyInvited: "{0} is al uitgenodigd voor het project",
          notMember: "{0} is geen lid van het project, dus kan je hun rol niet aanpassen",
          invalidRole: "{0} kan niet toegevoegd/verwijderd worden"
        }
      },
      success: {
        changedIcon: "Succesvol het projecticoon aangepast",
        resetIcon: "Succesvol het projecticoon gereset",
        rename: "Succesvol het project hernoemd naar {0}",
        softDelete: "Je hebt dit project verwijderd",
        hardDelete: "Je hebt dit project volledig verwijderd"
      },
      tabs: {
        general: "Algemeen",
        optional: "Optioneel",
        management: "Beheer",
        donation: "Donatie"
      }
    },
    discuss: {
      login: 'Log in',
      toReply: "om te reageren op deze discussie",
      noTopic: "Er is geen discussie voor dit project",
      send: "Reactie gepost!"
    }
  },
  page: {
    plural: "Pagina's",
    new: {
      title: "Creëer een nieuwe pagina",
      error: {
        minLength: "Paginainhoud is te kort",
        maxLength: "Paginainhoud is te lang",
        duplicateName: "Er bestaat al een pagina met die naam",
        invalidName: "Ongeldige naam",
        name: {
          maxLength: "Paginatitel te lang",
          minLength: "Paginatitel te kort",
          invalidChars: "Paginatitel bevat ongeldige tekens"
        },
        save: "Kon de pagina niet opslaan"
      },
      name: "Paginatitel",
      parent: "Ouderpagina (optioneel)"
    },
    delete: {
      title: "Verwijder pagina?",
      text: "Weet je zeker dat je de pagina wil verwijderen? Dit kan niet ongedaan gemaakt worden."
    }
  },
  version: {
    new: {
      title: "Creëer versie...",
      upload: "Upload Bestand",
      uploadNew: "Upload een nieuw versie",
      url: "Voer een URL in",
      form: {
        versionString: "Versie",
        fileName: "Bestandsnaam",
        fileSize: "Bestandsgrootte",
        externalUrl: "Externe URL",
        hangarProject: "Hangarproject",
        channel: "Kanaal",
        addChannel: "Voeg kanaal toe",
        unstable: "Instabiel",
        recommended: "Aanbevolen",
        forumPost: "Forumpost",
        release: {
          bulletin: "Bulletin vrijgeven",
          desc: "Wat is er nieuw in deze release?"
        },
        platforms: "Platformen",
        dependencies: "Pluginbenodigdheden"
      },
      error: {
        metaNotFound: "Kon metadata niet uit het geüploade bestand halen",
        jarNotFound: "Kon jar-bestand niet openen",
        fileExtension: "Incorrecte bestandsextensie",
        unexpected: "Een onverwachte fout trad op",
        invalidVersionString: "Ongeldige versiestring gevonden",
        duplicateNameAndPlatform: "Een versie met deze naam en compatibele platforms bestaat al",
        invalidNumOfPlatforms: "Ongeldig aantal platforms",
        duplicate: "Een versie met dit bestand bestaat al",
        noFile: "Kon geüploade bestand niet vinden",
        mismatchedFileSize: "Bestandsgrootten komen niet overeen",
        hashMismatch: "Bestandshashes komen niet overen",
        invalidPlatformVersion: "Ongeldige MC versie voor een platform aangegeven",
        fileIOError: "Bestand IO Fout",
        unknown: "Een onbekende fout trad op",
        incomplete: "Pluginbestand ontbreekt {0}",
        noDescription: "Moet een beschrijving hebben",
        invalidPluginDependencyNamespace: "Benoemde pluginbenodigdheid heeft een ongeldige projectnamespace",
        invalidName: "Ongeldige versie naam",
        channel: {
          noName: "Moet een kanaalnaam hebben",
          noColor: "Moet een kanaalkleur hebben"
        }
      }
    },
    edit: {
      platformVersions: "Bewerk Platformversies: {0}",
      pluginDeps: "Bewerk Pluginbenodigdheden: {0}",
      error: {
        noPlatformVersions: "Moet minstens één geldige platformversie toevoegen",
        invalidVersionForPlatform: "{0} is een ongeldige versie voor {1}",
        invalidProjectNamespace: "{0} is geen geldige projectnamespace"
      }
    },
    page: {
      subheader: "{0} heeft deze versie uitgebracht op {1}",
      dependencies: "Benodigdheden",
      platform: 'Platform',
      required: "(vereist)",
      adminMsg: "{0} keurde deze versie goed op {1}",
      reviewLogs: "Logboeken bekijken",
      reviewStart: "Review starten",
      setRecommended: "Zet als Aanbevolen",
      setRecommendedTooltip: "Zet deze versie als aanbevolen voor {0} platform",
      delete: "Verwijder",
      hardDelete: "Verwijder (voor altijd)",
      restore: "Herstel",
      download: "downloaden",
      downloadExternal: "Download Extern",
      adminActions: "Administratoracties",
      recommended: "Aanbevolen versie",
      partiallyApproved: "Deels goedgekeurd",
      approved: "Goedgekeurd",
      userAdminLogs: "Gebruikersadministratie Logs",
      unsafeWarning: "Deze versie is niet bekeken door ons moderatiepersoneel en kan misschien onveilig zijn om te downloaden.",
      downloadUrlCopied: "Gekopiëerd!",
      confirmation: {
        title: "Waarschuwing - {0} {1} door {2}",
        alert: "Deze versie is niet bekeken door ons moderatiepersoneel en kan misschien onveilig zijn om te downloaden.",
        disclaimer: "Disclaimer: We zijn niet aansprakelijk voor enige schade aan je systeem of server als je deze waarschuwing negeert.",
        agree: "Download op eigen risico",
        deny: "Ga terug"
      }
    },
    channels: "Kanalen",
    editChannels: "Bewerk Kanalen",
    platforms: "Platformen",
    error: {
      onlyOnePublic: "Je hebt maar 1 publieke versie over"
    },
    success: {
      softDelete: "Je hebt deze versie verwijderd",
      hardDelete: "Je hebt deze versie volledig verwijderd",
      restore: "Je hebt deze versie hersteld",
      recommended: "Je hebt deze versie ingesteld als aanbevolen voor {0} platform"
    }
  },
  channel: {
    modal: {
      titleNew: "Voeg een nieuw kanaal toe",
      titleEdit: "Bewerk kanaal",
      name: "Kanaalnaam",
      color: "Kanaalkleur",
      reviewQueue: "Buiten moderatiereviewrij houden?",
      error: {
        invalidName: "Ongeldige kanaalnaam",
        maxChannels: "Dit project heeft al het maximum aantal kanalen: {0}",
        duplicateColor: "Dit project heeft al een kanaal met deze kleur",
        duplicateName: "Dit project heeft al een kanaal met deze naam",
        tooLongName: "Kanaalnaam is te lang",
        cannotDelete: "Je kan dit kanaal niet verwijderen"
      }
    },
    manage: {
      title: "Releasekanalen",
      subtitle: "Releasekanalen laten de status van de release van een plugin zien. Een project mag 5 releasekanalen hebben.",
      channelName: "Kanaalnaam",
      versionCount: "Aantal Versies",
      reviewed: "Gereviewd",
      edit: "Bewerk",
      trash: "In de prullenbak gooien",
      editButton: "Bewerk",
      deleteButton: "Verwijder",
      add: "Voeg kanaal toe"
    }
  },
  organization: {
    new: {
      title: "Creëer een nieuwe Organisatie",
      text: "Organisaties zorgen ervoor dat je groep beter kan samenwerken aan je projecten op Hangar.",
      name: "Naam Organisatie",
      error: {
        duplicateName: "Er bestaat al een organisatie/gebruiker met die naam",
        invalidName: "Ongeldige organisatienaam",
        tooManyOrgs: "Je kan een maximum van {0} organisaties maken",
        notEnabled: "Organisaties zijn niet aan!",
        jsonError: "Fout tijdens het parsen van de JSON response van HangarAuth",
        hangarAuthValidationError: "Validatiefout: {0}",
        unknownError: "Onbekende fout tijdens maken organisatie"
      }
    },
    settings: {
      members: {
        invalidUser: "{0} is geen geldige gebruiken",
        alreadyInvited: "{0} is al uitgenodigd.",
        notMember: "{0} is geen lid van de organisatie dus je kan hun rol niet aanpassen",
        invalidRole: "{0} kan niet toegevoegd/verwijderd worden."
      }
    }
  },
  form: {
    memberList: {
      addUser: "Voeg gebruiker toe...",
      create: "Creëer",
      editUser: "Bewerk Gebruiker",
      invitedAs: "(Uigenodigd als {0})"
    }
  },
  notifications: {
    title: "Notificaties",
    invites: "Uitnodigingen",
    invited: "Je bent uitgenodigd voor {0}",
    inviteAccepted: "Je hebt de uitgenodiging voor {0} geaccepteerd",
    readAll: "Markeer alle als gelezen",
    unread: "Ongelezen",
    read: "Gelezen",
    all: "Alle",
    invite: {
      all: "Alle",
      projects: "Projecten",
      organizations: "Organisaties",
      btns: {
        accept: "Accepteer",
        decline: "Afwijzen",
        unaccept: "Onaccepteer"
      },
      msgs: {
        accept: "You bent nu lid van {0}",
        decline: "You hebt het lidmaatschap van {0} afgewezen",
        unaccept: "You hebt {0} verlaten"
      }
    },
    empty: {
      unread: "Je hebt geen ongelezen notificaties.",
      read: "Je hebt geen gelezen notificaties.",
      all: "Je hebt geen notificaties.",
      invites: "Je hebt geen uitnodigingen"
    },
    project: {
      reviewed: "{0} {1} is gereviewd en is goedgekeurd!",
      reviewedPartial: "{0} {1} is gereviewd en is deels goedgekeurd",
      newVersion: "Een nieuwe versie is uitgebracht voor {0}: {1}",
      invite: "Je bent uitgenodigd voor groep {0}  {1}",
      inviteRescinded: "Je uitnodiging voor groep {0} in project {1} is ingetrokken",
      removed: "Je bent uit group {0} in project {1} gezet",
      roleChanged: "Je bent toegevoegd aan groep {0} in project {1}"
    },
    organization: {
      invite: "Je bent uitgenodigd voor groep {0} in organisatie {1}",
      inviteRescinded: "Je uitnodiging voor {0} in organisatie {1} is ingetrokken",
      removed: "Je bent uit groep {0} in organisatie {1} gezet",
      roleChanged: "Je bent toegevoegd aan groep {0} in organisatie {1}"
    }
  },
  visibility: {
    notice: {
      new: "Dit project is nieuw, en zal niet voor anderen zichtbaar zijn totdat er een versie is geüpload. Als je te lang wacht met het uploaden van een versie, zal het project verwijderd worden.",
      needsChanges: "Dit project vereist veranderingen",
      needsApproval: "Je hebt het project ingediend voor review!",
      softDelete: "Project verwijderd door {0}"
    },
    name: {
      new: "Nieuw",
      public: "Publiek",
      needsChanges: "Veranderingen nodig",
      needsApproval: "Goedkeuring nodig",
      softDelete: "Zacht verwijderen"
    },
    changes: {
      version: {
        reviewed: "als gevolg van goedgekeurde beoordelingen"
      }
    },
    modal: {
      activatorBtn: "Zichtbaarheidsacties",
      title: "Verander {0}s zichtbaarheid",
      reason: "Reden voor verandering",
      success: "Je hebt de zichtbaarheid van {0}gewijzigd naar {1}"
    }
  },
  author: {
    watching: "Volgend",
    stars: "Sterren",
    orgs: "Organisaties",
    viewOnForums: "Bekijk op forum",
    taglineLabel: "Gebruikerstagline",
    editTagline: "Bewerk Tagline",
    memberSince: "Lid sinds {0}",
    numProjects: "Geen projecten | {0} project | {0} projecten",
    addTagline: "Voeg een tagline toe",
    noOrgs: "{0} is geen lid van een organisatie. 😢",
    noWatching: "{0} is geen enkel project aan het volgen. 😢",
    noStarred: "{0} heeft geen enkel project gesterd. 😢",
    tooltips: {
      settings: "Gebruikersinstellingen",
      lock: "Account Vergrendelen",
      unlock: "Account Ontgrendelen",
      apiKeys: "API sleutels",
      activity: "Gebruikersactiviteit",
      admin: "Gebruikersadministrator"
    },
    lock: {
      confirmLock: "Account van {0} vergrendelen?",
      confirmUnlock: "Account van {0} ontgrendelen?",
      successLock: "Het account van {0} is succesvol vergrendeld",
      successUnlock: "Het account van {0} is succesvol ontgrendeld"
    },
    org: {
      editAvatar: "Bewerk avatar"
    },
    error: {
      invalidTagline: "Ongeldige tagline",
      invalidUsername: "Ongeldige gebruikersnaam"
    }
  },
  linkout: {
    title: "Externe Link Waarschuwing",
    text: "Je hebt op een externe link naar \"{0}\" geklikt. Als dat niet de bedoeling was, ga dan terug. Als dat wel zo was, klik op verder.",
    abort: "Terug",
    continue: "Verder"
  },
  flags: {
    header: "Vlaggen voor",
    noFlags: "Geen vlaggen gevonden",
    resolved: "Ja, door {0} op {1}",
    notResolved: "Nee"
  },
  notes: {
    header: "Notities voor",
    noNotes: "Geen notities gevonden",
    addNote: "Notitie toevoegen",
    notes: "Notities",
    placeholder: "Notitie toevoegen..."
  },
  stats: {
    title: "Statistieken",
    plugins: "Plug-ins",
    reviews: "Beoordelingen",
    uploads: 'Uploads',
    downloads: "downloads",
    totalDownloads: "Totale Downloads",
    unsafeDownloads: "Onveilige Downloads",
    flags: "Vlaggen",
    openedFlags: "Geopende Vlaggen",
    closedFlags: "Gesloten Vlaggen"
  },
  health: {
    title: "Hangar Health Rapport",
    noTopicProject: "Ontbrekend discussieonderwerp",
    erroredJobs: "Gefaalde jobs",
    jobText: "Job type: {0}, Fout Type: {1}, Gebeurd: {2}",
    staleProjects: "Oude projectem",
    notPublicProjects: "Verborgen projecten",
    noPlatform: "Geen platform gedetecteerd",
    missingFileProjects: "Ontbrekend Bestand",
    empty: "Leeg! Alles prima!"
  },
  reviews: {
    headline: "{0} bracht deze versie uit op {1}",
    title: "Logboeken bekijken",
    projectPage: "Projectpagina",
    downloadFile: "Download Bestand",
    startReview: "Review starten",
    stopReview: "Stop de beoordeling",
    approve: "Keur goed",
    approvePartial: "Keur deels goed",
    notUnderReview: "Deze versie is niet onder review",
    reviewMessage: "Reviewbericht",
    addMessage: "Voeg bericht toe",
    reopenReview: "Heropen Review",
    undoApproval: "Goedkeuring ongedaan maken",
    hideClosed: "Alle afgemaakte reviews verbergen",
    error: {
      noReviewStarted: "Er is geen onafgemaakte review om een bericht aan toe te voegen",
      notCorrectUser: "Je bent niet de gebruiker die de review is begonnen",
      cannotReopen: "Kon de review niet openen",
      onlyOneReview: "Kan niet meer dan 1 review voor deze versie hebben",
      badUndo: "Kan goedkeuring alleen ongedaan maken na goedkeuring"
    },
    presets: {
      message: '{msg}',
      start: "{name} begon een review",
      stop: "{name} stopte met een review: {msg}",
      reopen: "{name} heropende een review",
      approve: "{name} keurde deze versie goed",
      approvePartial: "{name} keurde deze versie deels goed",
      undoApproval: "{name} heeft hun goedkeuring ongedaan gemaakt",
      reviewTitle: "{name}s Review"
    },
    state: {
      ongoing: "Bezig",
      stopped: "Gestopt",
      approved: "Goedgekeurd",
      partiallyApproved: "Deels Goedgekeurd",
      lastUpdate: "Laatste Update: {0}"
    }
  },
  apiKeys: {
    title: "API sleutels",
    createNew: "Creëer nieuwe key",
    existing: "Bestaande keys",
    name: "Naam",
    key: "Sleutel",
    keyIdentifier: "Key Identificatie",
    permissions: "Toestemmingen",
    delete: "Verwijder",
    deleteKey: "Verwijde Key",
    createKey: "Creëer key",
    noKeys: "Er zijn nog geen API keys. Je kan er een creëren aan de rechterkant",
    success: {
      delete: "Je hebt key {0} verwijderd",
      create: "Je hebt key {0} gecreërd"
    },
    error: {
      notEnoughPerms: "Onvoldoende toestemming om de key te maken",
      duplicateName: "Dubbele keynaam"
    }
  },
  apiDocs: {
    title: 'API Docs'
  },
  platformVersions: {
    title: "Configureer Platform Versies",
    platform: 'Platform',
    versions: "Versies",
    addVersion: "Voeg Versie Toe",
    saveChanges: "Veranderingen Opslaan",
    success: "Platformversies bijgewerkt"
  },
  flagReview: {
    title: "Vlaggen",
    noFlags: "Er zijn geen vlaggen om te reviewen.",
    msgUser: "Stuur gebruiker een bericht",
    msgProjectOwner: "Stuur eigenaar een bericht",
    markResolved: "Markeer als opgelost",
    line1: "{0} meldde {1} o[] {2}",
    line2: "Reden: {0}",
    line3: "Opmerking: {0}"
  },
  userActivity: {
    title: "Activiteit van {0}",
    reviews: "Beoordelingen",
    flags: "Vlaggen",
    reviewApproved: "Review Goedgekeurd",
    flagResolved: "Vlag Opgelost",
    error: {
      isOrg: "Kan activiteit voor organisatiegebruikers niet weergeven"
    }
  },
  userAdmin: {
    title: "Bewerk gebruiker",
    organizations: "Organisaties",
    organization: "Organisatie",
    projects: "Projecten",
    project: 'Project',
    owner: "Eigenaar",
    role: "Rol",
    accepted: "Geaccepteerd",
    sidebar: "Andere Administratie",
    hangarAuth: "HangarAuth-profiel",
    forum: "Forumprofiel"
  },
  userActionLog: {
    title: "Gebruikersactie Log",
    user: "Gebruiker",
    address: "IP Adres",
    time: "Tijd",
    action: "Actie",
    context: 'Context',
    oldState: "Oude Staat",
    newState: "Nieuwe Staat",
    markdownView: "Markdown-weergave",
    diffView: "Diff-weergave",
    types: {
      ProjectVisibilityChanged: "De zichtbaarheid van het project werd veranderd",
      ProjectRename: "Het project werd hernoemd",
      ProjectFlagged: "Het project werd gevlagd",
      ProjectSettingsChanged: "De projectinstellingen zijn veranderd",
      ProjectIconChanged: "Het projecticoon is veranderd",
      ProjectFlagResolved: "De vlag was opgelost",
      ProjectChannelCreated: "Een projectkanaal werd aangemaakt",
      ProjectChannelEdited: "Een projectkanaal werd bewerkt",
      ProjectChannelDeleted: "Een projectkanaal werd verwijderd",
      ProjectInvitesSent: "Projectuitnodigingen waren verzonden",
      ProjectInviteDeclined: "Een projectuitnodiging werd afgewezen",
      ProjectInviteUnaccepted: "Een projectuitnodiging werd ingetrokken",
      ProjectMemberAdded: "Een projectlid werd toegevoegd",
      ProjectMembersRemoved: "Projectleden waren verwijderd ",
      ProjectMemberRolesChanged: "De rollen van projectleden werden bijgewerkt",
      ProjectPageCreated: "Een projectpagina was aangemaakt",
      ProjectPageDeleted: "Een projectpagina was verwijderd",
      ProjectPageEdited: "Een projectpagina was bewerkt",
      VersionVisibilityChanged: "De zichtbaarheidsstatus van de versie is gewijzigd",
      VersionDeleted: "De versie was verwijderd",
      VersionCreated: "Een nieuwe versie werd geüpload",
      VersionDescriptionEdited: "De versiebeschrijving werd veranderd",
      VersionReviewStateChanged: "De reviewstatus van de versie werd veranderd",
      VersionPluginDependencyAdded: "Er is een pluginbenodigdheid toegevoegd",
      VersionPluginDependencyEdited: "Er is een pluginbenodigdheid bijgewerkt",
      VersionPluginDependencyRemoved: "Er is een pluginbenodigdheid verwijderd",
      VersionPlatformDependencyAdded: "Er is een platformbenodigdheid toegevoegd",
      VersionPlatformDependencyRemoved: "Er is een platformbenodigdheid verwijderd",
      UserTaglineChanged: "De tagline van de gebruiker is veranderd",
      UserLocked: "Deze gebruiker is vergrendeld",
      UserUnlocked: "Deze gebruiker is ontgrendeld",
      UserApikeyCreated: "Een apikey werd aangemaakt",
      UserApikeyDeleted: "Een apikey werd verwijderd",
      OrganizationInvitesSent: "Organisatie-uitnodigingen waren verstuurd",
      OrganizationInviteDeclined: "Een organisatie-uitnodiging werd afgewezen",
      OrganizationInviteUnaccepted: "Een organisatie-uitnodiging werd ingetrokken",
      OrganizationMemberAdded: "Een organisatielid werd toegevoegd",
      OrganizationMembersRemoved: "Organisatieleden werden verwijderd",
      OrganizationMemberRolesChanged: "De rollen van organisatieleden werden bijgewerkt"
    }
  },
  versionApproval: {
    title: "Versiegoedkeuringen",
    inReview: "Wordt beoordeeld",
    approvalQueue: "Goedkeuringswatchtrij",
    queuedBy: "In de rij gezet door",
    status: "status",
    project: 'Project',
    date: "Datum",
    version: "Versie",
    started: "Gestart: {0}",
    ended: "Geëindigd: {0}",
    statuses: {
      ongoing: "{0} bezig",
      stopped: "{0} gestopt",
      approved: "{0} goedgekeurd"
    }
  },
  projectApproval: {
    title: "Projectkeuringen",
    sendForApproval: "Je hebt het project ingediend voor goedkeuring",
    noProjects: "Geen projecten",
    needsApproval: "Goedkeuring Nodig",
    awaitingChanges: "Afwachten Veranderingen",
    description: "{0} vroeg om veranderingen op {1}"
  },
  donate: {
    title: "Doneer aan {}",
    monthly: "Maandelijks",
    oneTime: "Eenmalig",
    selectAmount: "Selecteer een aantal hierboven of kies zelf een hoeveelheid.",
    legal: "Door te doneren aan {0} ga je akkoord met Y en dat taco's overheerlijk zijn",
    cta: "Doneer",
    submit: "Doneer {0}"
  },
  lang: {
    button: "Verander taal",
    title: "Verander taal",
    available: "Beschikbaren talen",
    hangarAuth: "Dit verandert alleen - als een cookie - de locale van je browser. Klik hier om je taal te veranderen voor alle Paper-diensten"
  },
  validation: {
    required: "{0} is vereist",
    maxLength: "Maximum lengte is {0}",
    minLength: "Minimum lengte is {0}",
    invalidFormat: "{0} is ongeldig",
    invalidUrl: "Ongeldig URL-formaat"
  },
  prompts: {
    confirm: "Begrepen!",
    changeAvatar: {
      title: "Verander je avatar!",
      message: "Welkom bij je nieuwe organisatie! Begin met het veranderen van de avatar door er op te klikken."
    }
  },
  error: {
    userLocked: "Je account is vergrendeld.",
    401: "Je moet ingelogd zijn voor deze actie",
    403: "Je hebt geen toestemming om dat te doen",
    404: "404 Niet gevonden",
    unknown: "Er trad een onbekende fout op"
  }
};
export default msgs;
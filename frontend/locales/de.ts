import { LocaleMessageObject } from 'vue-i18n';
const msgs: LocaleMessageObject = {
  general: {
    close: "Schließen",
    submit: "Absenden",
    save: "Speichern",
    comment: "Kommentieren",
    change: "Ändern",
    donate: "Spenden",
    continue: "Weiter",
    create: "Anlegen",
    delete: "Löschen",
    or: "Oder",
    reset: 'Reset',
    edit: "Bearbeiten",
    required: "Benötigt",
    add: "Neu",
    name: 'Name',
    link: 'Link',
    send: "Senden",
    home: "Zuhause",
    message: "Nachricht",
    refresh: "Aktualisieren",
    confirm: "Bestätigen",
    error: {
      invalidUrl: "Ungültiges URL-Format"
    }
  },
  hangar: {
    projectSearch: {
      query: "Suche in {0} Projekten, die stolz von der Community gemacht wurden...",
      relevanceSort: "Nach Relevanz sortieren",
      noProjects: "Keine Projekte vorhanden. 😢",
      noProjectsFound: "0 Projekte gefunden. 😢"
    },
    subtitle: "Ein Minecraft-Paketrepository",
    sponsoredBy: "Gesponsert von"
  },
  pages: {
    staffTitle: "Mitarbeiter",
    authorsTitle: "Autoren",
    headers: {
      username: "Benutzername",
      roles: "Rollen",
      joined: "Beitreten",
      projects: "Projekte"
    }
  },
  nav: {
    login: "Anmelden",
    signup: "Anmelden",
    user: {
      notifications: "Benachrichtigungen",
      flags: "Flaggen",
      projectApprovals: "Projektgenehmigungen",
      versionApprovals: "Versionsgenehmigungen",
      stats: "Statistik",
      health: "Hangar Gesundheit",
      log: "Nutzer-Aktionsprotokoll",
      platformVersions: "Plattform-Versionen",
      logout: "Abmelden",
      error: {
        loginFailed: "Authentifizierung fehlgeschlagen",
        invalidUsername: "Ungültiger Benutzername",
        hangarAuth: "Verbindung zu HangarAuh fehlgeschlagen",
        loginDisabled: "Login ist vorübergehend nicht verfügbar, bitte versuchen Sie es später erneut",
        fakeUserEnabled: "Falschbenutzer ist aktiviert. {0} ist daher deaktiviert"
      }
    },
    createNew: "Neu erstellen...",
    new: {
      project: "Neues Projekt",
      organization: "Neue Organisation"
    },
    hangar: {
      home: 'Homepage',
      forums: "Foren",
      code: 'Code',
      docs: "Texte",
      javadocs: 'JavaDocs',
      hangar: 'Hangar (Plugins)',
      downloads: 'Downloads',
      community: 'Community'
    }
  },
  project: {
    stargazers: 'Stargazers',
    noStargazers: "Es gibt noch keine Sterngazer in diesem Projekt 😢",
    watchers: "Beobachter",
    noWatchers: "Es gibt noch keine Beobachter in diesem Projekt 😢",
    members: "Mitglieder",
    category: {
      info: "Kategorie: {0}",
      admin_tools: "Admin-Tools",
      chat: 'Chat',
      dev_tools: "Entwicklerwerkzeuge",
      economy: "Ökonomie",
      gameplay: "Spiel",
      games: "Spiele",
      protection: "Schutz",
      role_playing: "Rollenspiel",
      world_management: "Welt-Management",
      misc: "Sonstige"
    },
    actions: {
      unwatch: "Unbeobachten",
      watch: "Beobachten",
      flag: "Flagge",
      star: "Stern",
      unstar: "Stern",
      adminActions: "Admin-Aktionen",
      flagHistory: "Markierungsverlauf ({0})",
      staffNotes: "Mitarbeiternotizen ({0})",
      userActionLogs: "Nutzer-Aktionsprotokolle",
      forum: 'Forum'
    },
    flag: {
      flagProject: "{0} markieren?",
      flagSend: "Erfolgreich markiert, danke dafür, dass du diese Community zu einem besseren Ort gemacht hast!",
      flagSent: "Zur Überprüfung eingereichte Markierung",
      flags: {
        inappropriateContent: "Unangemessener Inhalt",
        impersonation: "Imitation oder Täuschung",
        spam: 'Spam',
        malIntent: "Bösartige Absicht",
        other: "Andere"
      },
      error: {
        alreadyOpen: "Sie können nur 1 ungelöstes Flag in einem Projekt haben",
        alreadyResolved: "Diese Flagge ist bereits gelöst"
      }
    },
    tabs: {
      docs: "Texte",
      versions: "Versionen",
      discuss: "Diskutieren",
      settings: "Einstellungen",
      homepage: 'Homepage',
      issues: "Probleme",
      source: "Quelle",
      support: "Unterstützung"
    },
    new: {
      step1: {
        title: "Benutzervereinbarung",
        text: "Ein Projekt enthält Ihre Downloads und die Dokumentation für Ihr Plugin.<br>Bevor Sie fortfahren, lesen Sie bitte die <a href=\"#\">Hangar Einreichungsrichtlinien.</a>",
        continue: "Zustimmen",
        back: "Abbrechen"
      },
      step2: {
        title: "Grundeinstellungen",
        continue: "Weiter",
        back: "Zurück",
        userSelect: "Erstellen als...",
        projectName: "Projektname",
        projectSummary: "Projektübersicht",
        projectCategory: "Projektkategorie"
      },
      step3: {
        title: "Zusätzliche Einstellungen",
        continue: "Weiter",
        back: "Zurück",
        optional: 'Optional',
        links: 'Links',
        homepage: 'Homepage',
        issues: "Issue-Tracker",
        source: "Quellcode",
        support: "Externer Support",
        license: "Lizenz",
        type: "Typ",
        customName: 'Name',
        url: 'URL',
        seo: 'SEO',
        keywords: "Stichwörter"
      },
      step4: {
        title: "Import von Spigot",
        continue: "Weiter",
        back: "Zurück",
        optional: 'Optional',
        convert: "Konvertieren",
        saveAsHomePage: "Als Startseite speichern",
        convertLabels: {
          bbCode: "BBCode hier einfügen",
          output: 'Markdown Output'
        },
        preview: "Vorschau",
        tutorial: "Wie man den BBCode bekommt",
        tutorialInstructions: {
          line1: "Um den BBCode deines Spigot Projekts zu erhalten, führe Folgendes aus:",
          line2: "1. Gehen Sie zu Ihrem Projekt und klicken Sie auf \"Ressourcen bearbeiten\".",
          line3: "2. Klicken Sie im Beschreibungseditor auf das Schraubenschlüssel.",
          line4: "Kopieren Sie den neuen Inhalt in das obere Konvertierer-Textfeld, führen Sie Änderungen an der Ausgabe durch, wenn Sie möchten, und drücken Sie \"speichern!"
        }
      },
      step5: {
        title: "Beendet",
        text: "Erstellen..."
      },
      error: {
        create: "Beim Erstellen des Projekts ist ein Fehler aufgetreten",
        nameExists: "Ein Projekt mit diesem Namen existiert bereits",
        slugExists: "Ein Projekt mit diesem Slug existiert bereits",
        invalidName: "Dieser Name enthält ungültige Zeichen",
        tooLongName: "Projektname ist zu lang",
        tooLongDesc: "Projektbeschreibung ist zu lang",
        tooManyKeywords: "Projekt hat zu viele Schlüsselwörter",
        noCategory: "Projekt muss eine Kategorie haben",
        noDescription: "Projekt muss eine Beschreibung haben"
      }
    },
    sendForApproval: "Zur Genehmigung senden",
    info: {
      title: "Informationen",
      publishDate: "Veröffentlicht am {0}",
      views: "0 Aufrufe | {0} Ansicht | {0} Aufrufe",
      totalDownloads: "0 insgesamt Downloads | {0} Gesamtdownload | {0} Gesamtdownloads",
      stars: "0 Sterne | {0} Sterne | {0} Sterne",
      watchers: "0 Beobachter | {0} Beobachter | {0} Beobachter"
    },
    promotedVersions: "Beworbene Versionen",
    license: {
      link: "Lizenziert unter "
    },
    error: {
      star: "Markierung konnte nicht umgeschaltet werden",
      watch: "Gesehen konnte nicht umschalten"
    },
    settings: {
      title: "Einstellungen",
      category: "Kategorie",
      categorySub: "Kategorisieren Sie Ihr Projekt in eine von 10 Kategorien. Die richtige Kategorisierung Ihres Projekts erleichtert die Suche nach Leuten.",
      keywords: "Stichwörter",
      keywordsSub: "Dies sind spezielle Wörter, die Ihr Projekt zurückgeben, wenn Leute sie zu ihrer Suche hinzufügen.",
      homepage: 'Homepage',
      homepageSub: "Eine eigene Homepage für Ihr Projekt hilft Ihnen, besser und offizieller zu sein und gibt Ihnen einen anderen Ort, um Informationen über Ihr Projekt zu sammeln.",
      issues: "Issue-Tracker",
      issuesSub: "Die Bereitstellung eines Fehlerverfolgungssystems hilft Ihren Benutzern die Unterstützung zu erleichtern und bietet Ihnen eine einfache Möglichkeit, Fehler zu verfolgen.",
      source: "Quellcode",
      sourceSub: "Unterstützen Sie die Entwicklergemeinschaft, indem Sie Ihr Projekt Open Source machen!",
      support: "Externe Unterstützung",
      supportSub: "Ein externer Ort, an dem du deinen Benutzern Unterstützung anbieten kannst. Kann ein Forum, ein Discord Server oder woanders sein.",
      license: "Lizenz",
      licenseSub: "Was können Leute mit Ihrem Projekt tun (und nicht?",
      forum: "Beiträge in den Foren erstellen",
      forumSub: "Legt fest, ob Ereignisse wie eine neue Version automatisch einen Beitrag in den Foren erstellen sollen",
      description: "Beschreibung",
      descriptionSub: "Eine kurze Beschreibung Ihres Projekts",
      icon: "Symbol",
      iconSub: "Laden Sie einen Bildvertreter Ihres Projekts hoch.",
      iconUpload: "Hochladen",
      iconReset: "Icon zurücksetzen",
      apiKey: "API-Schlüssel",
      apiKeySub: "Einen einzigartigen Bereitstellungsschlüssel generieren, um die Bereitstellung von Build von Gradle zu aktivieren",
      apiKeyGenerate: "Generieren",
      rename: "Umbenennen",
      renameSub: "Ändern des Projektnamens kann unerwünschte Folgen haben. Wir werden keine Weiterleitungen einrichten.",
      delete: "Löschen",
      deleteSub: "Sobald Sie ein Projekt löschen, kann es nicht wiederhergestellt werden.",
      hardDelete: "Harte Löschung",
      hardDeleteSub: "Sobald Sie ein Projekt löschen, kann es nicht wiederhergestellt werden. Für real dieses Mal...",
      save: "Änderungen speichern",
      optional: '(optional)',
      licenseCustom: "Eigener Name",
      licenseType: "Typ",
      licenseUrl: 'URL',
      donation: {
        enable: "Aktivieren",
        enableSub: "Aktiviere das Spendenformular für dieses Projekt",
        email: "E-Mail",
        emailSub: "Die E-Mail-Adresse des Paypal-Kontos, das die Spenden erhalten soll",
        defaultAmount: "Standardbetrag",
        defaultAmountSub: "Voreingestellter Standardbetrag",
        oneTimeAmounts: "Einmalige Beträge",
        oneTimeAmountsSub: "Liste der Optionen, die Sie Benutzern für einmalige Spenden geben möchten. Benutzer können immer benutzerdefinierte Beträge eingeben",
        monthlyAmounts: "Monatliche Beträge",
        monthlyAmountsSub: "Liste der Optionen, die Sie Benutzern für monatliche Spenden geben möchten. Benutzer können immer benutzerdefinierte Beträge eingeben"
      },
      error: {
        invalidFile: "{0} ist ein ungültiger Dateityp",
        noFile: "Keine Datei übermittelt",
        members: {
          invalidUser: "{0} ist kein gültiger Benutzer",
          alreadyInvited: "{0} ist bereits zum Projekt eingeladen",
          notMember: "{0} ist kein Mitglied des Projekts, daher kannst du seine Rolle nicht bearbeiten",
          invalidRole: "{0} kann nicht aus dem Projekt hinzugefügt oder entfernt werden"
        }
      },
      success: {
        changedIcon: "Projektsymbol erfolgreich geändert",
        resetIcon: "Projektsymbol erfolgreich zurückgesetzt",
        rename: "Das Projekt wurde erfolgreich in {0} umbenannt",
        softDelete: "Sie haben dieses Projekt gelöscht",
        hardDelete: "Sie haben dieses Projekt vollständig gelöscht"
      },
      tabs: {
        general: "Allgemein",
        optional: 'Optional',
        management: 'Management',
        donation: "Spende"
      }
    },
    discuss: {
      login: "Anmelden",
      toReply: "auf diese Diskussion antworten",
      noTopic: "Es gibt keine Diskussion für dieses Projekt",
      send: "Antwort gepostet!"
    }
  },
  page: {
    plural: "Seiten",
    new: {
      title: "Neue Seite erstellen",
      error: {
        minLength: "Seiteninhalte sind zu kurz",
        maxLength: "Seiteninhalt ist zu lang",
        duplicateName: "Eine Seite mit diesem Namen existiert bereits",
        invalidName: "Ungültiger Name",
        name: {
          maxLength: "Seitenname zu lang",
          minLength: "Seitenname zu kurz",
          invalidChars: "Seitenname enthielt ungültige Zeichen"
        },
        save: "Seite kann nicht gespeichert werden"
      },
      name: "Seitenname",
      parent: "Übergeordnete Seite (optional)"
    },
    delete: {
      title: "Seite löschen?",
      text: "Sind Sie sicher, dass Sie diese Seite löschen möchten? Dies kann nicht rückgängig gemacht werden."
    }
  },
  version: {
    new: {
      title: "Version erstellen...",
      upload: "Datei hochladen",
      uploadNew: "Neue Version hochladen",
      url: "URL eingeben",
      form: {
        versionString: 'Version',
        fileName: "Dateiname",
        fileSize: "Größe der Datei",
        externalUrl: "Externe URL",
        hangarProject: "Hangar Projekt",
        channel: "Kanal",
        addChannel: "Kanal hinzufügen",
        unstable: "Instabil",
        recommended: "Empfohlen",
        forumPost: "Forumbeitrag",
        release: {
          bulletin: "Bulletin freigeben",
          desc: "Was ist neu in dieser Version?"
        },
        platforms: "Plattformen",
        dependencies: "Plugin-Abhängigkeiten"
      },
      error: {
        metaNotFound: "Metadaten konnten nicht aus der hochgeladenen Datei geladen werden",
        jarNotFound: "Konnte jar Datei nicht öffnen",
        fileExtension: "Falsche Dateiendung",
        unexpected: "Unerwarteter Fehler aufgetreten",
        invalidVersionString: "Ungültige Versionszeichenfolge gefunden",
        duplicateNameAndPlatform: "Eine Version mit diesem Namen und kompatibler Plattform existiert bereits",
        invalidNumOfPlatforms: "Ungültige Anzahl von Plattformen",
        duplicate: "Eine Version mit dieser Datei existiert bereits",
        noFile: "Hochgeladene Datei konnte nicht gefunden werden",
        mismatchedFileSize: "Dateigrößen stimmen nicht überein",
        hashMismatch: "Datei-Hashes stimmen nicht überein",
        invalidPlatformVersion: "Ungültige MC-Version für eine Plattform angegeben",
        fileIOError: "Datei IO-Fehler",
        unknown: "Ein unbekannter Fehler ist aufgetreten",
        incomplete: "Plugin-Datei fehlt {0}",
        noDescription: "Muss eine Beschreibung haben",
        invalidPluginDependencyNamespace: "Deklarierte Plugin-Abhängigkeit hat einen ungültigen Projektnamespace",
        invalidName: "Ungültiger Versionsname",
        channel: {
          noName: "Muss einen Kanalnamen angegeben haben",
          noColor: "Muss eine Kanalfarbe angegeben haben"
        }
      }
    },
    edit: {
      platformVersions: "Plattform-Versionen bearbeiten: {0}",
      pluginDeps: "Plugin-Abhängigkeiten bearbeiten: {0}",
      error: {
        noPlatformVersions: "Mindestens eine gültige Plattformversion muss angegeben werden",
        invalidVersionForPlatform: "{0} ist eine ungültige Version für {1}",
        invalidProjectNamespace: "{0} ist kein gültiger Projekt-Namensraum"
      }
    },
    page: {
      subheader: "{0} hat diese Version auf {1} veröffentlicht",
      dependencies: "Abhängigkeiten",
      platform: "Plattform",
      required: "(erforderlich)",
      adminMsg: "{0} genehmigte diese Version auf {1}",
      reviewLogs: "Logs überprüfen",
      reviewStart: "Beginne Bewertung",
      setRecommended: "Als empfohlen festlegen",
      setRecommendedTooltip: "Diese Version als empfohlen für die Plattform {0} festlegen",
      delete: "Löschen",
      hardDelete: "Löschen (für immer)",
      restore: "Wiederherstellen",
      download: 'Download',
      downloadExternal: "Externe Downloads",
      adminActions: "Admin-Aktionen",
      recommended: "Empfohlene Version",
      partiallyApproved: "Teilweise genehmigt",
      approved: "Genehmigt",
      userAdminLogs: "Benutzer Admin-Logs",
      unsafeWarning: "Diese Version wurde von unserem Moderationspersonal nicht überprüft und kann nicht sicher heruntergeladen werden.",
      downloadUrlCopied: "Kopiert!",
      confirmation: {
        title: "Warnung - {0} {1} von {2}",
        alert: "Diese Version wurde von unserem Moderationspersonal noch nicht überprüft und ist möglicherweise nicht sicher zu benutzen.",
        disclaimer: "Haftungsausschluss: Wir lehnen jegliche Haftung für Schäden an Ihrem Server oder System ab, wenn Sie diese Warnung nicht beachten.",
        agree: "Download auf eigene Gefahr",
        deny: "Zurück"
      }
    },
    channels: "Kanäle",
    editChannels: "Kanäle bearbeiten",
    platforms: "Plattformen",
    error: {
      onlyOnePublic: "Sie haben nur noch eine öffentliche Version übrig"
    },
    success: {
      softDelete: "Sie haben diese Version gelöscht",
      hardDelete: "Sie haben diese Version vollständig gelöscht",
      restore: "Sie haben diese Version wiederhergestellt",
      recommended: "Sie haben diese Version als empfohlen für die Plattform {0} markiert"
    }
  },
  channel: {
    modal: {
      titleNew: "Neuen Kanal hinzufügen",
      titleEdit: "Kanal bearbeiten",
      name: "Kanalname",
      color: "Kanalfarbe",
      reviewQueue: "Warteschlange für Moderationsüberprüfungen ausschließen?",
      error: {
        invalidName: "Ungültiger Kanalname",
        maxChannels: "Dieses Projekt hat bereits die maximale Anzahl an Kanälen: {0}",
        duplicateColor: "Dieses Projekt hat bereits einen Kanal mit dieser Farbe",
        duplicateName: "Dieses Projekt hat bereits einen Kanal mit diesem Namen",
        tooLongName: "Kanalname ist zu lang",
        cannotDelete: "Dieser Kanal kann nicht gelöscht werden"
      }
    },
    manage: {
      title: "Release-Kanäle",
      subtitle: "Release-Kanäle stellen den Status einer Plugin-Version dar. Ein Projekt kann bis zu fünf Release-Kanäle haben.",
      channelName: "Kanalname",
      versionCount: "Versionsanzahl",
      reviewed: "Überprüft",
      edit: "Bearbeiten",
      trash: "Papierkorb",
      editButton: "Bearbeiten",
      deleteButton: "Löschen",
      add: "Kanal hinzufügen"
    }
  },
  organization: {
    new: {
      title: "Eine neue Organisation erstellen",
      text: "Organisationen ermöglichen es Ihnen, Benutzer zu gruppieren und eine engere Zusammenarbeit zwischen ihnen innerhalb Ihrer Projekte auf Hangar zu gewährleisten.",
      name: "Organisationsname",
      error: {
        duplicateName: "Eine Organisation/ein Benutzer mit diesem Namen existiert bereits",
        invalidName: "Ungültiger Organisationsname",
        tooManyOrgs: "Sie können maximal {0} Organisationen erstellen",
        notEnabled: "Organisationen sind nicht aktiviert!",
        jsonError: "Fehler beim Parsen der JSON-Antwort von HangarAuth",
        hangarAuthValidationError: "Validierungsfehler: {0}",
        unknownError: "Unbekannter Fehler beim Erstellen der Organisation"
      }
    },
    settings: {
      members: {
        invalidUser: "{0} ist kein gültiger Benutzer",
        alreadyInvited: "{0} ist bereits zur Organisation eingeladen",
        notMember: "{0} ist kein Mitglied der Organisation, daher kannst du seine Rolle nicht bearbeiten",
        invalidRole: "{0} kann nicht von der Organisation hinzugefügt / entfernt werden"
      }
    }
  },
  form: {
    memberList: {
      addUser: "Benutzer hinzufügen...",
      create: "Anlegen",
      editUser: "Benutzer bearbeiten",
      invitedAs: "(Eingeladen als {0})"
    }
  },
  notifications: {
    title: "Benachrichtigungen",
    invites: "Einladungen",
    invited: "Du wurdest eingeladen, der {0} beizutreten",
    inviteAccepted: "Sie haben eine Einladung an die {0} angenommen",
    readAll: "Alle als gelesen markieren",
    unread: "Ungelesen",
    read: "Lesen",
    all: "Alle",
    invite: {
      all: "Alle",
      projects: "Projekte",
      organizations: "Organisationen",
      btns: {
        accept: "Akzeptieren",
        decline: "Ablehnen",
        unaccept: 'Unaccept'
      },
      msgs: {
        accept: "Du bist {0} beigetreten",
        decline: "Du hast es abgelehnt {0} beizutreten",
        unaccept: "Sie haben {0} verlassen"
      }
    },
    empty: {
      unread: "Sie haben keine ungelesenen Benachrichtigungen.",
      read: "Du hast keine gelesenen Benachrichtigungen.",
      all: "Sie haben keine Benachrichtigungen.",
      invites: "Du hast keine Einladungen"
    },
    project: {
      reviewed: "{0} {1} wurde überprüft und genehmigt",
      reviewedPartial: "{0} {1} wurde überprüft und teilweise freigegeben",
      newVersion: "Eine neue Version wurde veröffentlicht für {0}: {1}",
      invite: "Sie wurden eingeladen, der Gruppe {0} im Projekt {1} beizutreten",
      inviteRescinded: "Ihre Einladung zur Gruppe {0} im Projekt {1} wurde aufgehoben",
      removed: "Sie wurden aus der Gruppe {0} im Projekt {1} entfernt",
      roleChanged: "Sie wurden zur Gruppe {0} im Projekt {1} hinzugefügt"
    },
    organization: {
      invite: "Sie wurden eingeladen, der Gruppe {0} in der Organisation {1} beizutreten",
      inviteRescinded: "Ihre Einladung zur Gruppe {0} in der Organisation {1} wurde aufgehoben",
      removed: "Sie wurden aus der Gruppe {0} in der Organisation {1} entfernt",
      roleChanged: "Sie wurden zur Gruppe {0} in der Organisation {1} hinzugefügt"
    }
  },
  visibility: {
    notice: {
      new: "Dieses Projekt ist neu und wird anderen erst angezeigt, wenn eine Version hochgeladen wurde. Wird eine Version nicht länger hochgeladen, wird das Projekt gelöscht.",
      needsChanges: "Dieses Projekt erfordert Änderungen",
      needsApproval: "Sie haben das Projekt zur Überprüfung gesendet",
      softDelete: "Projekt gelöscht von {0}"
    },
    name: {
      new: "Neu",
      public: "Öffentlich",
      needsChanges: "Benötigt Änderungen",
      needsApproval: "Genehmigung benötigt",
      softDelete: "Weich löschen"
    },
    changes: {
      version: {
        reviewed: "aufgrund genehmigter Bewertungen"
      }
    },
    modal: {
      activatorBtn: "Sichtbarkeitsaktionen",
      title: "{0}'s Sichtbarkeit ändern",
      reason: "Grund für Änderungen",
      success: "Sie haben die Sichtbarkeit von {0}zu {1} geändert"
    }
  },
  author: {
    watching: "Beobachten",
    stars: "Sterne",
    orgs: "Organisationen",
    viewOnForums: "In Foren anzeigen ",
    taglineLabel: "Benutzer-Schlagzeile",
    editTagline: "Bearbeite Tagline",
    memberSince: "Ein Mitglied seit {0}",
    numProjects: "Keine Projekte | {0} Projekt | {0} Projekte",
    addTagline: "Schlagzeile hinzufügen",
    noOrgs: "{0} ist nicht Teil einer Organisation. 😢",
    noWatching: "{0} beobachtet keine Projekte. 😢",
    noStarred: "{0}  hat keine Projekte markiert. 😢",
    tooltips: {
      settings: "Benutzereinstellungen",
      lock: "Konto sperren",
      unlock: "Konto entsperren",
      apiKeys: "API-Schlüssel",
      activity: "Benutzeraktivität",
      admin: "Benutzer Admin"
    },
    lock: {
      confirmLock: "Konto von {0}sperren?",
      confirmUnlock: "Konto von {0}entsperren?",
      successLock: "Konto von {0}erfolgreich gesperrt",
      successUnlock: "Konto von {0}erfolgreich entsperrt"
    },
    org: {
      editAvatar: "Avatar bearbeiten"
    },
    error: {
      invalidTagline: 'Invalid tagline',
      invalidUsername: "Ungültiger Benutzername"
    }
  },
  linkout: {
    title: "Warnung externer Link",
    text: "Sie haben auf einen externen Link zu \"{0}\" geklickt. Wenn Sie diesen Link nicht besuchen wollten, gehen Sie bitte zurück oder klicken Sie auf Fortfahren.",
    abort: "Zurück",
    continue: "Weiter"
  },
  flags: {
    header: "Flaggen für",
    noFlags: "Keine Flaggen gefunden",
    resolved: "Ja, von {0} am {1}",
    notResolved: "Nein"
  },
  notes: {
    header: "Notizen für",
    noNotes: "Keine Notizen gefunden",
    addNote: "Notiz hinzufügen",
    notes: "Notizen",
    placeholder: "Notiz hinzufügen..."
  },
  stats: {
    title: "Statistik",
    plugins: 'Plugins',
    reviews: "Bewertungen",
    uploads: 'Uploads',
    downloads: 'Downloads',
    totalDownloads: "Gesamte Downloads",
    unsafeDownloads: "Unsichere Downloads",
    flags: "Flaggen",
    openedFlags: "Geöffnete Flaggen",
    closedFlags: "Geschlossene Flaggen"
  },
  health: {
    title: "Hangar Gesundheitsbericht",
    noTopicProject: "Fehlendes Diskussionsthema",
    erroredJobs: "Fehlgeschlagene Jobs",
    jobText: "Job-Typ: {0}, Fehlertyp: {1}, ereignet: {2}",
    staleProjects: "Veraltete Projekte",
    notPublicProjects: "Versteckte Projekte",
    noPlatform: "Keine Plattform erkannt",
    missingFileProjects: "Fehlende Datei",
    empty: "Leer! Alles gut!"
  },
  reviews: {
    headline: "{0} hat diese Version auf {1} veröffentlicht",
    title: "Logs überprüfen",
    projectPage: "Projektseite",
    downloadFile: "Datei herunterladen",
    startReview: "Beginne Bewertung",
    stopReview: "Bewertung stoppen",
    approve: "Bestätigen",
    approvePartial: "Teilweise freigeben",
    notUnderReview: "Diese Version wird nicht überprüft",
    reviewMessage: "Nachricht überprüfen",
    addMessage: "Nachricht hinzufügen",
    reopenReview: "Rezension erneut öffnen",
    undoApproval: "Genehmigung rückgängig",
    hideClosed: "Alle abgeschlossenen Bewertungen ausblenden",
    error: {
      noReviewStarted: "Es gibt keine unfertige Überprüfung, um eine Nachricht hinzuzufügen",
      notCorrectUser: "Sie sind nicht der Benutzer, der diese Bewertung gestartet hat",
      cannotReopen: "Diese Bewertung konnte nicht erneut geöffnet werden",
      onlyOneReview: "Kann nicht mehr als 1 Bewertung für eine Version haben",
      badUndo: "Genehmigung nur nach Genehmigung rückgängig machen"
    },
    presets: {
      message: '{msg}',
      start: "{name} hat eine Bewertung begonnen",
      stop: "{name} hat eine Bewertung gestoppt: {msg}",
      reopen: "{name} hat eine Bewertung erneut geöffnet",
      approve: "{name} hat diese Version genehmigt",
      approvePartial: "{name} hat diese Version teilweise genehmigt",
      undoApproval: "{name} hat seine Genehmigung rückgängig gemacht",
      reviewTitle: "Bewertung von {name}"
    },
    state: {
      ongoing: "Laufend",
      stopped: "Stoppt",
      approved: "Genehmigt",
      partiallyApproved: "Teilweise genehmigt",
      lastUpdate: "Letzte Aktualisierung: {0}"
    }
  },
  apiKeys: {
    title: "API-Schlüssel",
    createNew: "Neuen Schlüssel erstellen",
    existing: "Vorhandene Schlüssel",
    name: 'Name',
    key: "Schlüssel",
    keyIdentifier: "Schlüssel-Identifikator",
    permissions: "Berechtigungen",
    delete: "Löschen",
    deleteKey: "Schlüssel löschen",
    createKey: "Schlüssel erstellen",
    noKeys: "Es gibt noch keine Api Keys. Sie können einen auf der rechten Seite erstellen",
    success: {
      delete: "Sie haben den Schlüssel gelöscht: {0}",
      create: "Sie haben den Schlüssel erstellt: {0}"
    },
    error: {
      notEnoughPerms: "Nicht genügend Berechtigungen, um diesen Schlüssel zu erstellen",
      duplicateName: "Doppelter Schlüsselname"
    }
  },
  apiDocs: {
    title: 'API Docs'
  },
  platformVersions: {
    title: "Plattform-Versionen konfigurieren",
    platform: "Plattform",
    versions: "Versionen",
    addVersion: "Version hinzufügen",
    saveChanges: "Änderungen speichern",
    success: "Aktualisierte Plattform-Versionen"
  },
  flagReview: {
    title: "Flaggen",
    noFlags: "Es gibt keine Flags zum Überprüfen.",
    msgUser: "Nachrichtenbenutzer",
    msgProjectOwner: "Nachrichteninhaber",
    markResolved: "Aufgelöst markieren",
    line1: "{0} hat {1} auf {2} gemeldet",
    line2: "Grund: {0}",
    line3: "Kommentar: {0}"
  },
  userActivity: {
    title: "Aktivität von {0}",
    reviews: "Bewertungen",
    flags: "Flaggen",
    reviewApproved: "Überprüfung genehmigt",
    flagResolved: "Markierung gelöst",
    error: {
      isOrg: "Aktivität für Organisationsbenutzer kann nicht angezeigt werden"
    }
  },
  userAdmin: {
    title: "Benutzer bearbeiten",
    organizations: "Organisationen",
    organization: "Organisation",
    projects: "Projekte",
    project: "Projekt",
    owner: "Besitzer",
    role: "Rolle",
    accepted: "Akzeptiert",
    sidebar: "Andere Administration",
    hangarAuth: "HangarAuth-Profil",
    forum: "Forum-Profil"
  },
  userActionLog: {
    title: "Nutzer-Aktionsprotokoll",
    user: "Benutzer",
    address: "IP-Adresse",
    time: "Zeit",
    action: "Aktion",
    context: "Kontext",
    oldState: "Alter Zustand",
    newState: "Neuer Status",
    markdownView: "Markdown Ansicht",
    diffView: "Diff-Ansicht",
    types: {
      ProjectVisibilityChanged: "Der Sichtbarkeitsstatus des Projekts wurde geändert",
      ProjectRename: "Das Projekt wurde umbenannt",
      ProjectFlagged: "Das Projekt wurde markiert",
      ProjectSettingsChanged: "Die Projekteinstellungen wurden geändert",
      ProjectIconChanged: "Das Projekt-Symbol wurde geändert",
      ProjectFlagResolved: "Die Fahne wurde gelöst",
      ProjectChannelCreated: "Ein Projektkanal wurde erstellt",
      ProjectChannelEdited: "Ein Projektkanal wurde bearbeitet",
      ProjectChannelDeleted: "Ein Projektkanal wurde gelöscht",
      ProjectInvitesSent: "Projekt-Einladungen wurden gesendet",
      ProjectInviteDeclined: "Eine Projekt-Einladung wurde abgelehnt",
      ProjectInviteUnaccepted: "Eine Projekteinladung wurde nicht angenommen",
      ProjectMemberAdded: "Ein Projektmitglied wurde hinzugefügt",
      ProjectMembersRemoved: "Projektmitglieder wurden entfernt",
      ProjectMemberRolesChanged: "Projektmitglieder wurden ihre Rollen aktualisiert",
      ProjectPageCreated: "Eine Projektseite wurde erstellt",
      ProjectPageDeleted: "Eine Projektseite wurde gelöscht",
      ProjectPageEdited: "Eine Projektseite wurde bearbeitet",
      VersionVisibilityChanged: "Der Sichtbarkeitsstatus der Version wurde geändert",
      VersionDeleted: "Die Version wurde gelöscht",
      VersionCreated: "Eine neue Version wurde hochgeladen",
      VersionDescriptionEdited: "Die Beschreibung der Version wurde bearbeitet",
      VersionReviewStateChanged: "Der Überprüfungsstatus der Version wurde geändert",
      VersionPluginDependencyAdded: "Eine Plugin-Abhängigkeit wurde hinzugefügt",
      VersionPluginDependencyEdited: "Eine Plugin-Abhängigkeit wurde bearbeitet",
      VersionPluginDependencyRemoved: "Eine Plugin-Abhängigkeit wurde entfernt",
      VersionPlatformDependencyAdded: "Eine Plattformabhängigkeit wurde hinzugefügt",
      VersionPlatformDependencyRemoved: "Eine Plattform-Abhängigkeit wurde entfernt",
      UserTaglineChanged: "Die Benutzer-Schlagzeile wurde geändert",
      UserLocked: "Dieser Benutzer ist gesperrt",
      UserUnlocked: "Dieser Benutzer ist entsperrt",
      UserApikeyCreated: "Ein Apikey wurde erstellt",
      UserApikeyDeleted: "Ein Apikey wurde gelöscht",
      OrganizationInvitesSent: "Organisationseinladungen wurden gesendet",
      OrganizationInviteDeclined: "Eine Einladung der Organisation wurde abgelehnt",
      OrganizationInviteUnaccepted: "Eine Einladung der Organisation wurde nicht angenommen",
      OrganizationMemberAdded: "Ein Organisationsmitglied wurde hinzugefügt",
      OrganizationMembersRemoved: "Organisationsmitglieder wurden entfernt",
      OrganizationMemberRolesChanged: "Organisationsmitglieder wurden ihre Rollen aktualisiert"
    }
  },
  versionApproval: {
    title: "Versionsgenehmigungen",
    inReview: "In Prüfung",
    approvalQueue: "Freigabe-Warteschlange",
    queuedBy: "Warteschlange von",
    status: 'Status',
    project: "Projekt",
    date: "Datum",
    version: 'Version',
    started: "Gestartet: {0}",
    ended: "Beendet: {0}",
    statuses: {
      ongoing: "{0} läuft",
      stopped: "{0} angehalten",
      approved: "{0} genehmigt"
    }
  },
  projectApproval: {
    title: "Projektgenehmigungen",
    sendForApproval: "Sie haben das Projekt zur Genehmigung gesendet",
    noProjects: "Keine Projekte",
    needsApproval: "Genehmigung benötigt",
    awaitingChanges: "Auf Änderungen warten",
    description: "{0} hat Änderungen am {1} angefordert"
  },
  donate: {
    title: "Spende an {}",
    monthly: "Monatlich",
    oneTime: "Einmal",
    selectAmount: "Wähle einen Betrag oben oder gebe einen Betrag unten ein",
    legal: "Durch die Spende an {0} stimmst du Y zu und diese Tacos sind lecker",
    cta: "Spenden",
    submit: "Spende {0}"
  },
  lang: {
    button: "Sprache wechseln",
    title: "Sprache wechseln",
    available: "Verfügbare Sprache",
    hangarAuth: "Dies ändert nur das Gebietsschema für Ihren aktuellen Browser (als Cookie). Klicken Sie hier, um Ihre Sprache auf Papier auth für alle Papierdienste zu ändern"
  },
  validation: {
    required: "{0} ist erforderlich",
    maxLength: "Maximale Länge ist {0}",
    minLength: "Minimale Länge ist {0}",
    invalidFormat: "{0} ist ungültig",
    invalidUrl: "Ungültiges URL-Format"
  },
  prompts: {
    confirm: "Verstanden!",
    changeAvatar: {
      title: "Ändere deinen Avatar!",
      message: "Willkommen in deiner neuen Organisation! Starte, indem du den Avatar änderst, indem du darauf klickst."
    }
  },
  error: {
    userLocked: "Ihr Konto ist gesperrt.",
    401: "Sie müssen eingeloggt sein für dieses",
    403: "Sie sind nicht berechtigt dies zu tun",
    404: "404 nicht gefunden",
    unknown: "Ein Fehler ist aufgetreten"
  }
};
export default msgs;
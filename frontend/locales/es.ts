import { LocaleMessageObject } from 'vue-i18n';
const msgs: LocaleMessageObject = {
  general: {
    close: "Cerrar",
    submit: "Enviar",
    save: "Guardar",
    comment: "Comentar",
    change: "Cambiar",
    donate: "Donar",
    continue: "Continuar",
    create: "Crear",
    delete: "Eliminar",
    or: "O",
    reset: "Reiniciar",
    edit: "Editar",
    required: "Requerido",
    add: "Añadir",
    name: "Nombre",
    link: "Enlace",
    send: "Enviar",
    home: "Inicio",
    message: "Mensaje",
    refresh: "Actualizar",
    confirm: "Confirmar",
    error: {
      invalidUrl: "Formato de URL inválido"
    }
  },
  hangar: {
    projectSearch: {
      query: "Buscar en {0} proyectos orgullosamente hechos por la comunidad...",
      relevanceSort: "Ordenar por importancia",
      noProjects: "No hay proyectos. 😢",
      noProjectsFound: "Se han encontrado 0 proyectos. 😢"
    },
    subtitle: "Un repositorio de paquetes de Minecraft",
    sponsoredBy: "Patrocinado por"
  },
  pages: {
    staffTitle: "Personal",
    authorsTitle: "Autores",
    headers: {
      username: "Nombre",
      roles: 'Roles',
      joined: "Se unió",
      projects: "Proyectos"
    }
  },
  nav: {
    login: "Inicio de sesión",
    signup: "Registro",
    user: {
      notifications: "Notificaciones",
      flags: "Marcas",
      projectApprovals: "Aprobaciones del proyecyo",
      versionApprovals: "Versiones aprobadas",
      stats: "Estadísticas",
      health: "Salud de Hangar",
      log: "Registro de las acciones del usuario",
      platformVersions: "Versiones de la plataforma",
      logout: "Cerrar sesión",
      error: {
        loginFailed: "Inicio de sesión fallido",
        invalidUsername: "Usuario inválido",
        hangarAuth: "No se puede conectar a HangarAuth",
        loginDisabled: "El reguistro esta desactivado temporalmente, intentalo de nuevo más tarde",
        fakeUserEnabled: "El usuario falso esta activado. Por tanto, {0} está desactivado"
      }
    },
    createNew: "Crear nuevo...",
    new: {
      project: "Nuevo proyecto",
      organization: "Nueva organización"
    },
    hangar: {
      home: "Inicio",
      forums: "Foros",
      code: "Código",
      docs: "Documentacion",
      javadocs: 'JavaDocs',
      hangar: 'Hangar (Plugins)',
      downloads: "Descargas",
      community: "Comunidad"
    }
  },
  project: {
    stargazers: 'Stargazers',
    noStargazers: "Todavía no hay stargazers en este proyecto 😢",
    watchers: "Notificados",
    noWatchers: "Todavía no hay notificados en este proyecto 😢",
    members: "Miembros",
    category: {
      info: "Categoría: {0}",
      admin_tools: "Herramientas de administración",
      chat: "Chatear",
      dev_tools: "Herramientas de desarrollo",
      economy: "Economía",
      gameplay: "Juego",
      games: "Juegos",
      protection: "Protección",
      role_playing: "Roleplay",
      world_management: "Administración de mundos",
      misc: "Miscelánea"
    },
    actions: {
      unwatch: "Dejar de seguir",
      watch: "Seguir",
      flag: "Marcar",
      star: "Estrella",
      unstar: "No estrella",
      adminActions: "Acciones de administrador",
      flagHistory: "Historial de reportes ({0})",
      staffNotes: "Notas del staff ({0})",
      userActionLogs: "Registros de acciones del usuario",
      forum: "Foro"
    },
    flag: {
      flagProject: "Reportar {0}?",
      flagSend: "Reportado exitosamente, ¡gracias por ayudar a hacer de esta comunidad un lugar mejor!",
      flagSent: "Reporte enviado para ser revisado",
      flags: {
        inappropriateContent: "Contenido inapropiado",
        impersonation: "Suplantacion de identidad",
        spam: 'Spam',
        malIntent: "Intereses maliciosos",
        other: "Otro"
      },
      error: {
        alreadyOpen: "Solo puedes tener un reporte abierto por proyecto",
        alreadyResolved: "Este reporte ya está resuelto"
      }
    },
    tabs: {
      docs: "Documentación",
      versions: "Versiones",
      discuss: "Discutir",
      settings: "Configuración",
      homepage: "Inicio",
      issues: "Problemas",
      source: "Código fuente",
      support: "Soporte"
    },
    new: {
      step1: {
        title: "Acuerdo de usuario",
        text: "Un proyecto contiene las descargas y la documentación de tu plugin.<br>Antes de continuar, por favor revisa las <a href=\"#\">Normas de proyectos de Hangar.</a>",
        continue: "Aceptar",
        back: "Abortar"
      },
      step2: {
        title: "Configuración básica",
        continue: "Continuar",
        back: "Atrás",
        userSelect: "Crear como...",
        projectName: "Nombre del proyecto",
        projectSummary: "Resumen del proyecto",
        projectCategory: "Categoría del proyecto"
      },
      step3: {
        title: "Configuración adicional",
        continue: "Continuar",
        back: "Atrás",
        optional: "Opcional",
        links: "Enlaces",
        homepage: "Página de inicio",
        issues: "Sistema de issues",
        source: "Código fuente",
        support: "Soporte externo",
        license: "Licencia",
        type: "Tipo",
        customName: "Nombre",
        url: 'URL',
        seo: 'SEO',
        keywords: "Palabras clave"
      },
      step4: {
        title: "Importar desde Spigot",
        continue: "Continuar",
        back: "Atrás",
        optional: "Opcional",
        convert: "Convertir",
        saveAsHomePage: "Guardar como página de inicio",
        convertLabels: {
          bbCode: "Pega tu BBCode aqui",
          output: "Salida de Markdown"
        },
        preview: "Previsualización",
        tutorial: "Como sacar el BBCode",
        tutorialInstructions: {
          line1: "Para sacar el BBCode de tu proyecto de Spigot, haz lo siguiente:",
          line2: "1. Ve a tu proyecto y haz click en \"Editar proyecto\".",
          line3: "2. Haz click en la llave inglesa en el editor de descripciones.",
          line4: "3. Copia los contenidos y pégalos en el campo de texto del editor de arriba, si quieres haz cambios a la salida, y ¡haz click en guardar!"
        }
      },
      step5: {
        title: "Terminando",
        text: "Creando..."
      },
      error: {
        create: "Hubo un error creando el proyecto",
        nameExists: "Ya existe un proyecto con este nombre",
        slugExists: "Ya existe un proyecto con este enlace",
        invalidName: "El nombre contiene caracteres inválidos",
        tooLongName: "El nombre del proyecto es demasiado largo",
        tooLongDesc: "La descripción del proyecto es demasiado larga",
        tooManyKeywords: "El proyecto tiene demasiadas palabras clave",
        noCategory: "El proyecto debe tener una categoría",
        noDescription: "El proyecto debe tener una descripción"
      }
    },
    sendForApproval: "Enviar para ser aprobado",
    info: {
      title: "Información",
      publishDate: "Publicado en {0}",
      views: "0 visitas | {0} visita | {0} visitas",
      totalDownloads: "0 descargas totales| {0} descarga total | {0} descargas totales",
      stars: "0 estrellas | {0} estrella | {0} estrellas",
      watchers: "0 vigilantes | {0} vigilantes | {0} vigilantes"
    },
    promotedVersions: "Versiones promovidas",
    license: {
      link: "Licenciado bajo "
    },
    error: {
      star: "No se pudo poner una estrella",
      watch: "No se pudo cambiar las notificaciones"
    },
    settings: {
      title: "Configuración",
      category: "Categoría",
      categorySub: "Categoriza tu proyecto en una de estas 10 categorías. Categorizando tu proyecto adecuadamente hace que se pueda encontrar más fácilmente",
      keywords: "Palabras clave",
      keywordsSub: "Palabras especiales que, cuando un usuario especifique en su búsqueda, haran que tu proyecto aparezca entre los resultados",
      homepage: "Página de inicio",
      homepageSub: "Tener una página de inicio personalizada para tu proyecto te ayuda a parecer más apropiado, oficial, y te da otro lugar para recolectar información sobre tu proyecto.",
      issues: "Rastreador de incidencias",
      issuesSub: "Proveer un Issue Tracker facilita estar al corriente de los bugs",
      source: "Código fuente",
      sourceSub: "¡Apoya a la comunidad de desarrolladores haciéndo tu proyecto de código abierto!",
      support: "Soporte externo",
      supportSub: "Un lugar externo donde puedes ofrecer soporte a tus usuarios. Podría ser un foro, un servidor de Discord, o realmente cualquier sitio",
      license: "Licencia",
      licenseSub: "¿Que puede la gente (no) hacer con tu proyecto?",
      forum: "Crear nuevos posts en los foros",
      forumSub: "Marca si algunos sucesos, como la publicacion de una nueva actualización han de crear un nuevo mensaje en el foro automáticamente",
      description: "Descripción",
      descriptionSub: "Una breve descripción de tu proyecto",
      icon: "Icono",
      iconSub: "Sube una imágen que represente tu proyecto.",
      iconUpload: "Subir",
      iconReset: "Resetear icono",
      apiKey: "Claves del API",
      apiKeySub: "Genera una clave de desplegado única para automaticamente publicar desde Gradle",
      apiKeyGenerate: "Generar",
      rename: "Renombrar",
      renameSub: "Cambiar el nombre de tu proyecto puede tener consecuencias indeseadas. No se añadirá ninguna redirección.",
      delete: "Borrar",
      deleteSub: "Cuando un proyecto se borra, no se puede recuperar.",
      hardDelete: "Borrar del todo",
      hardDeleteSub: "Cuando borras un proyecto se va para siempre",
      save: "Guardar cambios",
      optional: "(opcional)",
      licenseCustom: "Nombre personalizado",
      licenseType: "Tipo",
      licenseUrl: 'URL',
      donation: {
        enable: "Activar",
        enableSub: "Activar el formulario de donaciones para este proyecto",
        email: "E-mail",
        emailSub: "El correo electrónico de la cuenta de PayPal donde deben llegar las donaciones",
        defaultAmount: "Cantidad por defecto",
        defaultAmountSub: "La cantidad por defecto preseleccionada",
        oneTimeAmounts: "Cantidades de una vez",
        oneTimeAmountsSub: "Las opciones que quieres dar a usuarios que quieren donar solamente una vez. Los usuarios siempre pueden añadir otras cantidades",
        monthlyAmounts: "Cantidades mensuales",
        monthlyAmountsSub: "Las opciones que los usuarios tiene para donaciones mensuales. Los usuarios siempre pueden añadir otras cantidades"
      },
      error: {
        invalidFile: "El tipo de archivo {0} es inválido",
        noFile: "No has enviado un archivo",
        members: {
          invalidUser: "{0} no es un usuario válido",
          alreadyInvited: "{0} ya está invitado al proyecto",
          notMember: "{0} no es un miembro del proyecto y por ello no puedes editar su rol",
          invalidRole: "{0} no puede ser añadido/eliminado del proyecto"
        }
      },
      success: {
        changedIcon: "Se ha cambiado el icono del proyecto exitosamente",
        resetIcon: "Se ha reseteado el icono del proyecto exitosamente",
        rename: "Se ha cambiado el nombre del proyecto a {0} exitosamente",
        softDelete: "Has borrado este proyecto",
        hardDelete: "Has borrado este proyecto totalmente"
      },
      tabs: {
        general: 'General',
        optional: "Opcional",
        management: "Administración",
        donation: "Donación"
      }
    },
    discuss: {
      login: "Inicia sesion",
      toReply: "para responder a este conversación",
      noTopic: "No hay conversación en este proyecto",
      send: "¡Respuesta publicada!"
    }
  },
  page: {
    plural: "Páginas",
    new: {
      title: "Crea una nueva página",
      error: {
        minLength: "Los contenidos de la página son demasiado cortos",
        maxLength: "Los contenidos de la página son demasiado largos",
        duplicateName: "Ya hay una página con ese nombre",
        invalidName: "Nombre inválido",
        name: {
          maxLength: "El nombre de la página es demasiado largo",
          minLength: "El nombre de la página es demasiado corto",
          invalidChars: "El nombre de la página contiene caracteres inválidos"
        },
        save: "No se ha podido guardar la página"
      },
      name: "Nombre de la página",
      parent: "Página Superior (opcional)"
    },
    delete: {
      title: "¿Eliminar página?",
      text: "¿Seguro que quieres eliminar esta página? Esta operación no se puede deshacer."
    }
  },
  version: {
    new: {
      title: "Crear versión...",
      upload: "Subir archivo",
      uploadNew: "Subir una nueva versión",
      url: "Escribe una URL",
      form: {
        versionString: "Versión",
        fileName: "Nombre del archivo",
        fileSize: "Tamaño del archivo",
        externalUrl: "URL Externa",
        hangarProject: "Proyecto Hangar",
        channel: "Canal",
        addChannel: "Añadir canal",
        unstable: "Inestable",
        recommended: "Recomendado",
        forumPost: "Post en el foro",
        release: {
          bulletin: "Boletín de nueva versión",
          desc: "¿Que hay de nuevo en esta versión?"
        },
        platforms: "Platformas",
        dependencies: "Dependencias del plugin"
      },
      error: {
        metaNotFound: "No se ha podido leer la metadata del archivo subido",
        jarNotFound: "No se ha podido abrir el jar",
        fileExtension: "Extensión de archivo incorrecta",
        unexpected: "Un error inesperado ha sucedido",
        invalidVersionString: "Texto de versión incorrecto",
        duplicateNameAndPlatform: "Ya existe una version con este nombre para esta plataforma",
        invalidNumOfPlatforms: "Número de plataformas inválido",
        duplicate: "Ya existe una versión con este archivo",
        noFile: "No se pudo encontrar el archivo subido",
        mismatchedFileSize: "El tamaño de los archivos no coincided",
        hashMismatch: "Los hashes del archivo no coinciden",
        invalidPlatformVersion: "Versioón de Minecraft inválida para la plataforma especificada",
        fileIOError: "Error en el IO del archivo",
        unknown: "Un error desconocido ha ocurrido",
        incomplete: "Falta el archivo del plugin {0}",
        noDescription: "Tienes que añadir una descripción",
        invalidPluginDependencyNamespace: "Una dependencia declarada tiene un nombre de proyecto inválido",
        invalidName: "Nombre de la versión inválido",
        channel: {
          noName: "Debes especificar un nombre para el canal",
          noColor: "Debes especificar un color para el canal"
        }
      }
    },
    edit: {
      platformVersions: "Editar las versiones de las plataformas: {0}",
      pluginDeps: "Editar las dependencias: {0}",
      error: {
        noPlatformVersions: "Debes especificar al menos una versión de alguna plataforma",
        invalidVersionForPlatform: "{0} es una versión inválida de {1}",
        invalidProjectNamespace: "{0} es un nombre inválido"
      }
    },
    page: {
      subheader: "{0} publicó esta version el {1}",
      dependencies: "Dependencias",
      platform: "Platforma",
      required: "(requerido)",
      adminMsg: "{0} aprobó esta versión en {1}",
      reviewLogs: "Ver registros",
      reviewStart: "Iniciar reseña",
      setRecommended: "Marcar como recomendado",
      setRecommendedTooltip: "Marcar esta version como la recomendada para la plataforma {0}",
      delete: "Eliminar",
      hardDelete: "Eliminar (para siempre)",
      restore: "Restaurar",
      download: "Descargar",
      downloadExternal: "Descarga externa",
      adminActions: "Acciones de administrador",
      recommended: "Versión recomendada",
      partiallyApproved: "Parcialmente aprobado",
      approved: "Aprobado",
      userAdminLogs: "Registros de administración del usuario",
      unsafeWarning: "Esta versión no ha sido revisada por nuestros moderadores y podría ser peligrosa",
      downloadUrlCopied: "¡Copiado!",
      confirmation: {
        title: "Advertencia - {0} {1} por {2}",
        alert: "Esta versión todavía no ha sido revisada por nuestros moderadores y podría ser insegura.",
        disclaimer: "Renuncia de responsabilidad: No nos responsabilizamos de cualquier daño que esta descarga pueda producir en tu servidor o sistema si decides ignorar esta advertencia.",
        agree: "Descárgalo bajo tu propia responsabilidad",
        deny: "Atrás"
      }
    },
    channels: "Canales",
    editChannels: "Editar canales",
    platforms: "Platformas",
    error: {
      onlyOnePublic: "Solo te queda una versión pública"
    },
    success: {
      softDelete: "Has eliminado esta versión",
      hardDelete: "Has eliminado completamente esta versión",
      restore: "Has restaurado esta versión",
      recommended: "Has marcado esta versión como recomendada para la plataforma {0}"
    }
  },
  channel: {
    modal: {
      titleNew: "Añadir un nuevo canal",
      titleEdit: "Editar canal",
      name: "Nombre del canal",
      color: "Color del canal",
      reviewQueue: "¿Excluir de la cola de revisión de moderación?",
      error: {
        invalidName: "Nombre de canal inválido",
        maxChannels: "Este proyecto ya tiene el número máximo de canales: {0}",
        duplicateColor: "Este proyecto ya tiene un canal con este color",
        duplicateName: "Este proyecto ya tiene un canal con este nombre",
        tooLongName: "El nombre del canal es demasiado largo",
        cannotDelete: "No puedes eliminar este canal"
      }
    },
    manage: {
      title: "Liberar canales",
      subtitle: "Los canales de lanzamiento representan el estado de una versión de plugin. Un proyecto puede tener hasta cinco canales de publicación.",
      channelName: "Nombre del canal",
      versionCount: "Número de versiones",
      reviewed: "Revisado",
      edit: "Editar",
      trash: "Basura",
      editButton: "Editar",
      deleteButton: "Eliminar",
      add: "Añadir canal"
    }
  },
  organization: {
    new: {
      title: "Crear una nueva organización",
      text: "Organizations allow you group users provide closer collaboration between them within your projects on Hangar.",
      name: "Nombre de la organización",
      error: {
        duplicateName: "Ya existe una organización/usuario con ese nombre",
        invalidName: "Nombre de organización no válido",
        tooManyOrgs: "Sólo puede crear un máximo de {0} organizaciones",
        notEnabled: "¡Las organizaciones no están habilitadas!",
        jsonError: "Error al analizar la respuesta JSON de HangarAuth",
        hangarAuthValidationError: "Error de validación: {0}",
        unknownError: "Error desconocido al crear la organización"
      }
    },
    settings: {
      members: {
        invalidUser: "{0} no es un usuario válido",
        alreadyInvited: "{0} ya está invitado a la organización",
        notMember: "{0} no es miembro de la organización, por lo tanto no puedes editar su rol",
        invalidRole: "{0} no puede ser añadido/eliminado de la organización"
      }
    }
  },
  form: {
    memberList: {
      addUser: "Añadir usuario...",
      create: "Crear",
      editUser: "Editar usuario",
      invitedAs: "(Invitado como {0})"
    }
  },
  notifications: {
    title: "Notificaciones",
    invites: "Invitaciones",
    invited: "Has sido invitado a unirte a {0}",
    inviteAccepted: "Usted ha aceptado una invitación a {0}",
    readAll: "Marcar todo como leído",
    unread: "No leídos",
    read: "Leer",
    all: "Todos",
    invite: {
      all: "Todos",
      projects: "Proyectos",
      organizations: "Organizaciones",
      btns: {
        accept: "Aceptar",
        decline: "Rechazar",
        unaccept: 'Unaccept'
      },
      msgs: {
        accept: "Te has unido a {0}",
        decline: "Has rechazado unirte a {0}",
        unaccept: "Has abandonado {0}"
      }
    },
    empty: {
      unread: "No tienes notificaciones sin leer.",
      read: "No tienes notificaciones leídas.",
      all: "No tienes notificaciones.",
      invites: "No tienes invitaciones"
    },
    project: {
      reviewed: "{0} {1} ha sido revisado y está aprobado",
      reviewedPartial: "{0} {1} ha sido revisado y está parcialmente aprobado",
      newVersion: "Una nueva versión ha sido liberada para {0}: {1}",
      invite: "Has sido invitado a unirte al grupo {0} en el proyecto {1}",
      inviteRescinded: "Su invitación al grupo {0} en el proyecto {1} ha sido rescindido",
      removed: "Usted ha sido eliminado del grupo {0} en el proyecto {1}",
      roleChanged: "Has sido añadido al grupo {0} en el proyecto {1}"
    },
    organization: {
      invite: "Has sido invitado a unirte al grupo {0} en la organización {1}",
      inviteRescinded: "Su invitación a usted el grupo {0} en la organización {1} ha sido rescindido",
      removed: "Has sido eliminado del grupo {0} en la organización {1}",
      roleChanged: "Has sido añadido al grupo {0} en la organización {1}"
    }
  },
  visibility: {
    notice: {
      new: "Este proyecto es nuevo y no se mostrará a otros hasta que se suba una versión. Si una versión no se carga durante más tiempo, el proyecto será eliminado.",
      needsChanges: "Este proyecto requiere cambios",
      needsApproval: "Ha enviado el proyecto para su revisión",
      softDelete: "Proyecto eliminado por {0}"
    },
    name: {
      new: "Nuevo",
      public: "Público",
      needsChanges: "Necesita cambios",
      needsApproval: "Necesita aprobación",
      softDelete: "Borrar Soft"
    },
    changes: {
      version: {
        reviewed: "debido a revisiones aprobadas"
      }
    },
    modal: {
      activatorBtn: "Acciones de visibilidad",
      title: "Cambiar visibilidad de {0}",
      reason: "Motivo del cambio",
      success: "Has cambiado la visibilidad de {0}a {1}"
    }
  },
  author: {
    watching: "Viendo",
    stars: "Estrellas",
    orgs: "Organizaciones",
    viewOnForums: "Ver en foros ",
    taglineLabel: "Etiqueta de usuario",
    editTagline: "Editar Tagline",
    memberSince: "Un miembro desde {0}",
    numProjects: "Ningún proyecto | {0} proyecto | {0} proyectos",
    addTagline: "Añadir una etiqueta",
    noOrgs: "{0} no forma parte de ninguna organización. 😢",
    noWatching: "{0} no está viendo ningún proyecto. 😢",
    noStarred: "{0}  no ha marcado ningún proyecto. 😢",
    tooltips: {
      settings: "Ajustes de usuario",
      lock: "Bloquear cuenta",
      unlock: "Desbloquear cuenta",
      apiKeys: "Claves del API",
      activity: "Actividad del usuario",
      admin: "Administrador"
    },
    lock: {
      confirmLock: "¿Bloquear cuenta de {0}?",
      confirmUnlock: "¿Desbloquear la cuenta de {0}?",
      successLock: "La cuenta de {0}ha sido bloqueada con éxito",
      successUnlock: "Cuenta de {0}desbloqueada correctamente"
    },
    org: {
      editAvatar: "Editar avatar"
    },
    error: {
      invalidTagline: 'Invalid tagline',
      invalidUsername: "Nombre de usuario inválido"
    }
  },
  linkout: {
    title: "Advertencia de enlace externo",
    text: "Ha hecho clic en un enlace externo a \"{0}\". Si no tiene intención de visitar este enlace, por favor regrese. De lo contrario, haga clic en continuar.",
    abort: "Volver",
    continue: "Continuar"
  },
  flags: {
    header: "Banderas para",
    noFlags: "No se encontraron banderas",
    resolved: "Sí, por {0} en {1}",
    notResolved: "Nu"
  },
  notes: {
    header: "Notas para",
    noNotes: "Notas no encontradas",
    addNote: "Añadir nota",
    notes: "Notas",
    placeholder: "Añadir una nota..."
  },
  stats: {
    title: "Estadísticas",
    plugins: 'Plugins',
    reviews: "Reseñas",
    uploads: "Subir",
    downloads: "Descargas",
    totalDownloads: "Descargas totales",
    unsafeDownloads: "Descargas inseguras",
    flags: "Marcas",
    openedFlags: "Banderas abiertas",
    closedFlags: "Banderas cerradas"
  },
  health: {
    title: "Informe de salud de Hangar",
    noTopicProject: "Falta tema de discusión",
    erroredJobs: "Tareas fallidas",
    jobText: "Tipo de trabajo: {0}, Tipo de error: {1}, Happened: {2}",
    staleProjects: "Proyectos obsoletos",
    notPublicProjects: "Proyectos ocultos",
    noPlatform: "Plataforma no detectada",
    missingFileProjects: "Falta archivo",
    empty: "¡Vacío! ¡Todo bien!"
  },
  reviews: {
    headline: "{0} publicó esta version el {1}",
    title: "Ver registros",
    projectPage: "Página del proyecto",
    downloadFile: "Descargar archivo",
    startReview: "Comenzar revisión",
    stopReview: "Detener revisión",
    approve: "Aprobar",
    approvePartial: "Aprobar Parcial",
    notUnderReview: "Esta versión no está siendo revisada",
    reviewMessage: "Revisar mensaje",
    addMessage: "Añadir mensaje",
    reopenReview: "Reabrir revisión",
    undoApproval: "Deshacer aprobación",
    hideClosed: "Ocultar todas las reseñas terminadas",
    error: {
      noReviewStarted: "No hay ninguna revisión sin finalizar para agregar un mensaje",
      notCorrectUser: "Usted no es el usuario que inició esta revisión",
      cannotReopen: "No se puede reabrir esta revisión",
      onlyOneReview: "No se puede tener más de 1 reseña para una versión",
      badUndo: "Sólo se puede deshacer la aprobación después de una aprobación"
    },
    presets: {
      message: '{msg}',
      start: "{name} comenzó una reseña",
      stop: "{name} detuvo una reseña: {msg}",
      reopen: "{name} reabrió una reseña",
      approve: "{name} aprobó esta versión",
      approvePartial: "{name} ha aprobado parcialmente esta versión",
      undoApproval: "{name} ha deshecho su aprobación",
      reviewTitle: "Revisión de {name}"
    },
    state: {
      ongoing: "En curso",
      stopped: "Detenido",
      approved: "Aprobado",
      partiallyApproved: "Parcialmente aprobado",
      lastUpdate: "Última actualización: {0}"
    }
  },
  apiKeys: {
    title: "Claves del API",
    createNew: "Crear nueva clave",
    existing: "Claves existentes",
    name: "Nombre",
    key: "Clave",
    keyIdentifier: "Identificador de clave",
    permissions: "Permisos",
    delete: "Eliminar",
    deleteKey: "Borrar Clave",
    createKey: "Crear clave",
    noKeys: "No hay claves api todavía. Puedes crear una en el lado derecho",
    success: {
      delete: "Ha eliminado la clave: {0}",
      create: "Ha creado la clave: {0}"
    },
    error: {
      notEnoughPerms: "No hay suficientes permisos para crear esa clave",
      duplicateName: "Nombre de clave duplicado"
    }
  },
  apiDocs: {
    title: 'API Docs'
  },
  platformVersions: {
    title: "Configurar versiones de plataforma",
    platform: "Platforma",
    versions: "Versiones",
    addVersion: "Añadir versión",
    saveChanges: "Guardar Cambios",
    success: "Versiones de plataforma actualizadas"
  },
  flagReview: {
    title: "Marcas",
    noFlags: "No hay banderas que revisar.",
    msgUser: "Usuario de mensaje",
    msgProjectOwner: "Propietario del mensaje",
    markResolved: 'Mark resolved',
    line1: '{0} reported {1} on {2}',
    line2: 'Reason: {0}',
    line3: 'Comment: {0}'
  },
  userActivity: {
    title: "{0}'s Activity",
    reviews: 'Reviews',
    flags: "Marcas",
    reviewApproved: 'Review Approved',
    flagResolved: 'Flag Resolved',
    error: {
      isOrg: 'Cannot show activity for organization users'
    }
  },
  userAdmin: {
    title: 'Edit User',
    organizations: 'Organizations',
    organization: 'Organization',
    projects: "Proyectos",
    project: 'Project',
    owner: 'Owner',
    role: 'Role',
    accepted: 'Accepted',
    sidebar: 'Other Administration',
    hangarAuth: 'HangarAuth Profile',
    forum: 'Forum Profile'
  },
  userActionLog: {
    title: "Registro de las acciones del usuario",
    user: 'User',
    address: 'IP Address',
    time: 'Time',
    action: 'Action',
    context: 'Context',
    oldState: 'Old State',
    newState: 'New State',
    markdownView: 'Markdown View',
    diffView: 'Diff View',
    types: {
      ProjectVisibilityChanged: 'The project visibility state was changed',
      ProjectRename: 'The project was renamed',
      ProjectFlagged: 'The project got flagged',
      ProjectSettingsChanged: 'The project settings were changed',
      ProjectIconChanged: 'The project icon was changed',
      ProjectFlagResolved: 'The flag was resolved',
      ProjectChannelCreated: 'A project channel was created',
      ProjectChannelEdited: 'A project channel was edited',
      ProjectChannelDeleted: 'A project channel was deleted',
      ProjectInvitesSent: 'Project invites were sent',
      ProjectInviteDeclined: 'A project invite was declined',
      ProjectInviteUnaccepted: 'A project invite was unaccepted',
      ProjectMemberAdded: 'A project member was added',
      ProjectMembersRemoved: 'Project members were removed',
      ProjectMemberRolesChanged: 'Project members had their roles updated',
      ProjectPageCreated: 'A project page was created',
      ProjectPageDeleted: 'A project page was deleted',
      ProjectPageEdited: 'A project page was edited',
      VersionVisibilityChanged: "The version's visibility state was changed",
      VersionDeleted: 'The version was deleted',
      VersionCreated: 'A new version was uploaded',
      VersionDescriptionEdited: 'The version description was edited',
      VersionReviewStateChanged: "The version's review state was changed",
      VersionPluginDependencyAdded: 'A plugin dependency was added',
      VersionPluginDependencyEdited: 'A plugin dependency was edited',
      VersionPluginDependencyRemoved: 'A plugin dependency was removed',
      VersionPlatformDependencyAdded: 'A platform dependency was added',
      VersionPlatformDependencyRemoved: 'A platform dependency was removed',
      UserTaglineChanged: 'The user tagline changed',
      UserLocked: 'This user is locked',
      UserUnlocked: 'This user is unlocked',
      UserApikeyCreated: 'An apikey was created',
      UserApikeyDeleted: 'An apikey was deleted',
      OrganizationInvitesSent: 'Organization invites were sent',
      OrganizationInviteDeclined: 'An organization invite was declined',
      OrganizationInviteUnaccepted: 'An organization invite was unaccepted',
      OrganizationMemberAdded: 'An organization member was added',
      OrganizationMembersRemoved: 'Organization members were removed',
      OrganizationMemberRolesChanged: 'Organization members had their roles updated'
    }
  },
  versionApproval: {
    title: 'Version Approvals',
    inReview: 'In Review',
    approvalQueue: 'Approval queue',
    queuedBy: 'Queued by',
    status: 'Status',
    project: 'Project',
    date: 'Date',
    version: "Versión",
    started: 'Started: {0}',
    ended: 'Ended: {0}',
    statuses: {
      ongoing: '{0} ongoing',
      stopped: '{0} stopped',
      approved: '{0} approved'
    }
  },
  projectApproval: {
    title: 'Project Approvals',
    sendForApproval: 'You have sent the project for approval',
    noProjects: 'No projects',
    needsApproval: 'Needs Approval',
    awaitingChanges: 'Awaiting Changes',
    description: '{0} requested changes on {1}'
  },
  donate: {
    title: 'Donate to {}',
    monthly: 'Monthly',
    oneTime: 'One-Time',
    selectAmount: 'Select an amount above or enter an amount below',
    legal: 'By donating to {0} you agree to Y and that tacos are delicious',
    cta: "Donar",
    submit: 'Donate {0}'
  },
  lang: {
    button: 'Switch Language',
    title: 'Switch Language',
    available: 'Available Language',
    hangarAuth: 'This only change the locale for your current browser (as a cookie). Click here to change your lang on paper auth for all paper services'
  },
  validation: {
    required: '{0} is required',
    maxLength: 'Maximum length is {0}',
    minLength: 'Minimum length is {0}',
    invalidFormat: '{0} is invalid',
    invalidUrl: "Formato de URL inválido"
  },
  prompts: {
    confirm: 'Got it!',
    changeAvatar: {
      title: 'Change your avatar!',
      message: "Welcome to your new organization! Start by changing it's avatar by clicking on it."
    }
  },
  error: {
    userLocked: 'Your account is locked.',
    401: 'You must be logged in for this',
    403: 'You do not have permission to do that',
    404: '404 Not found',
    unknown: 'An error occurred'
  }
};
export default msgs;
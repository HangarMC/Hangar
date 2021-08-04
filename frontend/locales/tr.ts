import { LocaleMessageObject } from 'vue-i18n';
const msgs: LocaleMessageObject = {
  general: {
    close: "Kapat",
    submit: "Gönder",
    save: "Kaydet",
    comment: "Yorum yap",
    change: "Değiştir",
    donate: "Bağışta bulun",
    continue: "Devam et",
    create: "Oluştur",
    delete: "Sil",
    or: "Veya",
    reset: "Sıfırla",
    edit: "Düzenle",
    required: "Gerekli",
    add: "Ekle",
    name: "Ad",
    link: "Bağlantı",
    send: "Gönder",
    home: "Anasayfa",
    message: "Mesaj Gönder",
    refresh: "Yenile",
    confirm: "Doğrula",
    error: {
      invalidUrl: "Geçersiz URL formatı"
    }
  },
  hangar: {
    projectSearch: {
      query: "Topluluk tarafından gururla tasarlanmış {0} proje içinde ara...",
      relevanceSort: "Alaka düzeyine göre sırala",
      noProjects: "Proje yok. 😢",
      noProjectsFound: "0 proje bulundu. 😢"
    },
    subtitle: "Bir Minecraft paket deposu.",
    sponsoredBy: "Sponsor: "
  },
  pages: {
    staffTitle: "Personel",
    authorsTitle: "Yazarlar",
    headers: {
      username: "Kullanıcı adı",
      roles: "Roller",
      joined: "Katıldı",
      projects: "Projeler"
    }
  },
  nav: {
    login: "Giriş yap",
    signup: "Kayıt yap",
    user: {
      notifications: "Bildirimler",
      flags: "Etiketler",
      projectApprovals: "Proje onayları",
      versionApprovals: "Versiyon onayları",
      stats: "İstatikler",
      health: "Hangar Sağlığı",
      log: "Kullanıcı eylem geçmişi",
      platformVersions: "Platform Versiyonları",
      logout: "Çıkış yap",
      error: {
        loginFailed: "Kimlik doğrulama Başarısız",
        invalidUsername: "Geçersiz Kullanıcı adı",
        hangarAuth: "HangarAuth'a bağlanılamadı",
        loginDisabled: "Giriş geçici olarak kullanılamıyor, lütfen daha sonra tekrar deneyin",
        fakeUserEnabled: "Sahte kullanıcı etkinleştirildi. {0} bu nedenle devre dışı bırakıldı"
      }
    },
    createNew: "Yeni oluştur...",
    new: {
      project: "Yeni Proje",
      organization: "Yeni Kuruluş"
    },
    hangar: {
      home: "Anasayfa",
      forums: "Forumlar",
      code: "Kod",
      docs: "Dokümentasyon",
      javadocs: "JavaDocları",
      hangar: "Hangar (Pluginler)",
      downloads: "İndirmeler",
      community: "Topluluk"
    }
  },
  project: {
    stargazers: "Yıldız gözlemcileri",
    noStargazers: "Bu projenin hiç yıldız gözlemcisi yok 😢",
    watchers: "İzleyenler",
    noWatchers: "Bu projeyi izleyen yok 😢",
    members: "Üyeler",
    category: {
      info: "Kategori: {0}",
      admin_tools: "Yönetici Araçları",
      chat: "Sohbet",
      dev_tools: "Geliştirici Araçları",
      economy: "Ekonomi",
      gameplay: "Oynanış",
      games: "Oyunlar",
      protection: "Koruma",
      role_playing: "Rol Yapma",
      world_management: "Dünya Yönetimi",
      misc: "Çeşitli"
    },
    actions: {
      unwatch: "İzlemeyi bırak",
      watch: "İzle",
      flag: "Etiketle",
      star: "Yıldızla",
      unstar: "Yıldızı kaldır",
      adminActions: "Yönetici seçenekleri",
      flagHistory: "Etiket geçmişi ({0})",
      staffNotes: "Personel notları ({0})",
      userActionLogs: "Kullanıcı eylem geçmişi",
      forum: 'Forum'
    },
    flag: {
      flagProject: "{0} etiketlensin mi?",
      flagSend: "Başarıyla etiketlendi, bu topluluğu daha iyi bir yer yaptığınız için teşekkürler!",
      flagSent: "Etiket inceleme için gönderildi",
      flags: {
        inappropriateContent: "Uygunsuz İçerik",
        impersonation: "Taklit etme veya Aldatma",
        spam: "Spam (Aşırı tekrar eden içerik)",
        malIntent: "Kötü Amaç",
        other: "Diğer"
      },
      error: {
        alreadyOpen: 'You can only have 1 unresolved flag on a project',
        alreadyResolved: 'This flag is already resolved'
      }
    },
    tabs: {
      docs: "Dokümentasyon",
      versions: "Versiyonlar",
      discuss: "Tartış",
      settings: "Ayarlar",
      homepage: "Anasayfa",
      issues: "Sorunlar",
      source: "Kaynak",
      support: "Destek"
    },
    new: {
      step1: {
        title: "Kullanıcı Anlaşması",
        text: "Bir proje, sizin indirmelerinizi ve sizin eklentinizin belgelerini içerir.<br>Devam etmeden önce lütfen <a href=\"#\">Hangar Gönderim Yönergelerine.</a> bir göz atın.",
        continue: "Kabul ediyorum",
        back: "İptal"
      },
      step2: {
        title: "Basit Ayarlar",
        continue: "Devam",
        back: "Geri",
        userSelect: "Kullanıcı seç",
        projectName: "Proje Adı",
        projectSummary: "Proje Özeti",
        projectCategory: "Proje Kategorisi"
      },
      step3: {
        title: "Ek Ayarlar",
        continue: "Devam",
        back: "Geri",
        optional: "İsteğe Bağlı",
        links: "Bağlantılar",
        homepage: "Anasayfa",
        issues: "Sorun İzleyici",
        source: "Kaynak Kodu",
        support: "Harici Destek",
        license: "Lisans",
        type: "Tip",
        customName: "Ad",
        url: 'URL',
        seo: 'SEO',
        keywords: "Anahtar Kelimeler"
      },
      step4: {
        title: "Spigottan İçe aktar",
        continue: "Devam",
        back: "Geri",
        optional: "İsteğe Bağlı",
        convert: "Dönüştür",
        saveAsHomePage: "Ana Sayfa olarak kaydet",
        convertLabels: {
          bbCode: "BBKodunu buraya yapıştır",
          output: "Markdown Çıktısı"
        },
        preview: "Önizleme",
        tutorial: "BBKod nasıl alınır",
        tutorialInstructions: {
          line1: "Spigot projenizin BBKodunu almak için şunları yapın:",
          line2: "1. Projenize gidin ve \"Edit Resource (Kaynağı Düzenle)\" yazan yere basın.",
          line3: "2. Açıklama düzenleyicide İngiliz anahtarı sembolüne tıklayın.",
          line4: "3. Kopyala yeni içeriği üst dönüştürücü metin kutusuna yapıştırın, isterseniz çıktıda değişiklik yapın ve kaydete basın!"
        }
      },
      step5: {
        title: "Bitiriliyor",
        text: "Oluşturuluyor..."
      },
      error: {
        create: "Proje oluşturulurken bir hata gerçekleşti",
        nameExists: "Bu isimde bir proje zaten var",
        slugExists: "Bu harf dizisi ile bir proje zaten var",
        invalidName: "Bu isim geçersiz karakterler içeriyor",
        tooLongName: "Proje adı çok uzun",
        tooLongDesc: "Proje açıklaması çok uzun",
        tooManyKeywords: "Proje çok fazla anahtar kelime içeriyor",
        noCategory: "Proje bir kategoriye dahil olmalıdır",
        noDescription: "Projenin bir açıklaması olmalıdır"
      }
    },
    sendForApproval: "Onay için gönder",
    info: {
      title: "Detaylar",
      publishDate: "{0} tarihinde paylaşıldı",
      views: "0 görüntüleme | {0} görüntüleme | {0} görüntüleme",
      totalDownloads: "0 toplam indirme | {0} toplam indirme | {0} toplam indirme",
      stars: "0 yıldız | {0} yıldız | {0} yıldız",
      watchers: "0 izleyici | {0} izleyici | {0} izleyici"
    },
    promotedVersions: "Tanıtılan Sürümler",
    license: {
      link: "Lisans: "
    },
    error: {
      star: "Yıldızlanmışlar değiştirilemedi",
      watch: "İzlenenler değiştirilemedi"
    },
    settings: {
      title: "Ayarlar",
      category: "Kategori",
      categorySub: "Projenizi 10 kategoriden birine ayırın. Projenizi uygun şekilde kategorize etmek, insanların bulmasını kolaylaştırır.",
      keywords: "Anahtar kelimeler",
      keywordsSub: "Bunlar, insanlar onları aramalarına eklediğinde insanları projenize yönlendirecek özel kelimelerdir.",
      homepage: "Anasayfa",
      homepageSub: "Projeniz için özel bir ana sayfaya sahip olmak, daha düzgün, resmi görünmenize yardımcı olur ve projeniz hakkında bilgi toplamak için size başka bir yer sağlar.",
      issues: 'Issue tracker',
      issuesSub: 'Providing an issue tracker helps your users get support more easily and provides you with an easy way to track bugs.',
      source: "Kaynak Kodu",
      sourceSub: "Projenizi açık kaynak yaparak geliştiriciler topluluğunu destekleyin!",
      support: "Harici Destek",
      supportSub: "Kullanıcılarınıza destek sunabileceğiniz harici bir yer. Bir forum, Discord sunucusu veya başka bir yer olabilir.",
      license: "Lisans",
      licenseSub: "İnsanlar projenizi nasıl kullanabilirler? (Ve nasıl kullanamazlar.)",
      forum: "Forumda gönderi oluşturun",
      forumSub: "Yeni bir sürüm gibi durumlarda forumlarda otomatik olarak bir gönderi oluşturup oluşturmayacağını ayarlar",
      description: "Açıklama",
      descriptionSub: "Projeniz hakkında kısa bir açıklama.",
      icon: "Simge",
      iconSub: "Projenizi temsil eden bir resim yükleyin",
      iconUpload: "Yükle",
      iconReset: "Simgeyi",
      apiKey: "API Anahtarları",
      apiKeySub: "Gradle ile derleme dağıtımını etkinleştirmek için benzersiz bir dağıtım anahtarı oluşturun",
      apiKeyGenerate: "Oluştur",
      rename: "Yeniden adlandır",
      renameSub: "Projenizin adını değiştirmek istenmeyen sonuçlara yol açabilir. Herhangi bir yeniden yönlendirme ayarlamayacağız.",
      delete: "Sil",
      deleteSub: "Bir projeyi sildikten sonra geri alınamaz.",
      hardDelete: "Kesin Silme",
      hardDeleteSub: "Bir projeyi sildikten sonra geri alınamaz. Bu sefer gerçekten alınamaz...",
      save: "Değişiklikleri kaydet",
      optional: "(isteğe bağlı)",
      licenseCustom: "Ad",
      licenseType: "Tip",
      licenseUrl: 'URL',
      donation: {
        enable: "Etkinleştir",
        enableSub: "Bu proje için bağış formunu etkinleştirin",
        email: "E-Posta",
        emailSub: "Bağışları alması gereken paypal hesabının E-Posta adresi",
        defaultAmount: "Varsayılan Tutar",
        defaultAmountSub: "Önceden seçilmiş varsayılan bağış miktarı",
        oneTimeAmounts: "Tek Seferlik Tutarlar",
        oneTimeAmountsSub: "Kullanıcılara tek seferlik bağışlar için vermek istediğiniz seçeneklerin listesi. Kullanıcılar her zaman özel bir tutar girebilir",
        monthlyAmounts: "Aylık Tutarlar",
        monthlyAmountsSub: "Aylık bağışlar için kullanıcılara sunmak istediğiniz seçeneklerin listesi. Kullanıcılar her zaman özel bir tutar girebilir"
      },
      error: {
        invalidFile: "{0} geçersiz bir dosya tipi",
        noFile: "Dosya yüklenmedi",
        members: {
          invalidUser: "{0} geçerli bir kullanıcı değil",
          alreadyInvited: "{0} zaten bu projeye davet edildi",
          notMember: "{0} projenin bir üyesi değil, bu nedenle rollerini düzenleyemezsiniz",
          invalidRole: "{0} bu projeye eklenemez ve bu projeden kaldırılamaz"
        }
      },
      success: {
        changedIcon: "Proje simgesi başarıyla değiştirildi",
        resetIcon: "Proje simgesi başarıyla sıfırlandı",
        rename: "Projenin adı {0} olarak değiştirildi",
        softDelete: "Bu projeyi sildiniz",
        hardDelete: "Bu projeyi tamamen sildiniz"
      },
      tabs: {
        general: "Genel",
        optional: "İsteğe Başlı",
        management: "Yönetim",
        donation: "Bağış"
      }
    },
    discuss: {
      login: "Giriş yap",
      toReply: "bu tartışmaya cevap vermek için",
      noTopic: "Bu proje için bir tartışma yok",
      send: "Cevap gönderildi!"
    }
  },
  page: {
    plural: "Sayfalar",
    new: {
      title: "Yeni bir sayfa oluştur",
      error: {
        minLength: "Sayfa içerikleri çok kısa",
        maxLength: "Sayfa içerikleri çok uzun",
        duplicateName: "Bu ad ile bir sayfa zaten var",
        invalidName: "Geçersiz isim",
        name: {
          maxLength: "Sayfa adı çok kısa",
          minLength: "Sayfa adı çok uzun",
          invalidChars: "Sayfa adı geçersiz karakterler içeriyor"
        },
        save: "Sayfa kaydedilemedi"
      },
      name: "Sayfa Adı",
      parent: "Ana Sayfa (isteğe bağlı)"
    },
    delete: {
      title: "Sayfa silisin mi?",
      text: "Sayfa silisin mi? Bu işlem geri alınamaz!"
    }
  },
  version: {
    new: {
      title: "Versiyon oluştur...",
      upload: "Dosya Yükle",
      uploadNew: "Yeni bir Versiyon Yükle",
      url: "Bir URL girin",
      form: {
        versionString: "Versiyon",
        fileName: "Dosya adı",
        fileSize: "Dosya boyutu",
        externalUrl: "Harici URL",
        hangarProject: "Hangar Projesi",
        channel: "Kanal",
        addChannel: "Kanal Ekle",
        unstable: "Dengesiz",
        recommended: "Önerilen",
        forumPost: "Forum Gönderisi",
        release: {
          bulletin: "Yayın Bülteni",
          desc: "Bu sürümdeki yenilikler neler?"
        },
        platforms: "Platforlar",
        dependencies: "Eklenti Gereksinimleri"
      },
      error: {
        metaNotFound: "Yüklenen dosyadan metaveriler yüklenemedi",
        jarNotFound: "Jar dosyası açılamadı",
        fileExtension: "Hatalı dosya uzantısı",
        unexpected: "Beklenmedik bir hata gerçekleşti",
        invalidVersionString: "Geçersiz sürüm dizesi bulundu",
        duplicateNameAndPlatform: "Bu ada ve uyumlu platforma sahip bir sürüm zaten var",
        invalidNumOfPlatforms: "Geçersiz sayıda platform",
        duplicate: "Bu dosyaya sahip bir sürüm zaten var",
        noFile: "Yüklenen dosya bulunamadı",
        mismatchedFileSize: "Dosya boyutları uyuşmadı",
        hashMismatch: "Dosya karma değerleri (hash) eşleşmiyor",
        invalidPlatformVersion: "Belirtilen bir platform için geçersiz MC sürümü",
        fileIOError: "Dosya IO Hatası",
        unknown: "Bilinmeyen bir hata gerçekleşti",
        incomplete: "Plugin dosyasında {0} eksik",
        noDescription: "Bir açıklama bulunmalı",
        invalidPluginDependencyNamespace: "Bildirilen eklenti gereksinimi geçersiz bir proje ad alanına (namespace) sahip",
        invalidName: "Geçersiz versiyon adı",
        channel: {
          noName: "Bir kanal adı  belirtilmelidir",
          noColor: "Bir kanal rengi belirtilmelidir"
        }
      }
    },
    edit: {
      platformVersions: "Platform Sürümlerini Düzenle: {0}",
      pluginDeps: "Eklenti Gereksinimlerini Düzenle: {0}",
      error: {
        noPlatformVersions: "En az bir geçerli platform sürümü sağlanmalıdır",
        invalidVersionForPlatform: "{0}, {1} için geçersiz bir sürüm",
        invalidProjectNamespace: "{0} geçerli bir proje ad alanı (namespace) değil"
      }
    },
    page: {
      subheader: "{0}, bu sürümü {1} tarihinde yayınladı",
      dependencies: "Gereksinimler",
      platform: 'Platform',
      required: "(zorunlu)",
      adminMsg: "{0}, bu sürümü {1} tarihinde onayladı",
      reviewLogs: "Günlükleri inceleyin",
      reviewStart: "İnceleme Başlat",
      setRecommended: "Önerilen olarak ayarla",
      setRecommendedTooltip: "Bu sürümü {0} platformu için önerilen şekilde ayarlayın",
      delete: "Sil",
      hardDelete: "Sil (sonsuza dek)",
      restore: "Geri getir",
      download: "İndir",
      downloadExternal: "Harici İndir",
      adminActions: "Yönetici eylemleri",
      recommended: "Önerilen versiyon",
      partiallyApproved: "Kısmen onaylandı",
      approved: "Onaylandı",
      userAdminLogs: "Kullanıcı Yönetici Günlükleri",
      unsafeWarning: "Bu sürüm, denetleme ekibimiz tarafından incelenmemiştir ve indirilmesi güvenli olmayabilir.",
      downloadUrlCopied: "Kopyalandı!",
      confirmation: {
        title: "Uyarı - {0} {2} tarafından {1}",
        alert: "Bu sürüm, denetleme ekibimiz tarafından henüz incelenmedi ve kullanımı güvenli olmayabilir.",
        disclaimer: "Sorumluluk Reddi: Bu uyarıyı dikkate almamayı seçerseniz, sunucunuza veya sisteminize gelebilecek herhangi bir zararın sorumluluğunu reddediyoruz..",
        agree: "Sorumluluğu kabul editorum, indir",
        deny: "Geri git"
      }
    },
    channels: "Kanallar",
    editChannels: "Kanalları Düzenle",
    platforms: "Platformlar",
    error: {
      onlyOnePublic: "Yalnızca 1 genel sürümünüz kaldı"
    },
    success: {
      softDelete: "Bu versiyonu sildiniz.",
      hardDelete: "Bu versiyonu tamamen sildiniz",
      restore: "Bu versiyonu geri getirdiniz",
      recommended: "Bu sürümü {0} platformu için önerilen olarak işaretlediniz"
    }
  },
  channel: {
    modal: {
      titleNew: "Yeni bir kanal ekle",
      titleEdit: "Kanalı düzenle",
      name: "Kanal Adı",
      color: "Kanal Rengi",
      reviewQueue: "Denetleme inceleme kuyruğundan hariç tutulsun mu?",
      error: {
        invalidName: "Geçersiz kanal adı",
        maxChannels: "Bu proje zaten maksimum kanal sayısına sahip: {0}",
        duplicateColor: "Bu proje zaten bu renkte bir kanala sahip",
        duplicateName: "Bu projede zaten bu ada sahip bir kanal var",
        tooLongName: "Kanal adınız çok uzun",
        cannotDelete: "Bu kanalı silemezsiniz"
      }
    },
    manage: {
      title: "Yayın kanalları",
      subtitle: "Sürüm kanalları, bir eklenti sürümünün durumunu temsil eder. Bir projede en fazla beş yayın kanalı olabilir.",
      channelName: "Kanal Adı",
      versionCount: "Sürüm Sayısı",
      reviewed: "İncelendi",
      edit: "Düzenle",
      trash: "Çöp",
      editButton: "Düzenle",
      deleteButton: "Sil",
      add: "Kanal Ekle"
    }
  },
  organization: {
    new: {
      title: "Bir Kuruluş kur",
      text: "Kuruluşlar, Hangar'daki projeleriniz dahilinde kullanıcıları gruplamanıza ve aralarında daha yakın işbirliği sağlamanıza olanak tanır.",
      name: "Kuruluş adı",
      error: {
        duplicateName: "Bu ada sahip bir Kuruluş/kullanıcı zaten var",
        invalidName: "Geçersiz Kuruluş adı.",
        tooManyOrgs: "En fazla {0} Kuruluş oluşturabilirsiniz",
        notEnabled: "Kuruluşlar etkin değil!",
        jsonError: "HangarAuth'tan JSON yanıtı ayrıştırılırken hata oluştu",
        hangarAuthValidationError: "Doğrulama Hatası: {0}",
        unknownError: "Organizasyon oluşturulurken bilinmeyen hata"
      }
    },
    settings: {
      members: {
        invalidUser: "{0} geçerli bir kullanıcı değil",
        alreadyInvited: "{0} zaten Kuruluşa davet edildi",
        notMember: "{0} Kuruluşun bir üyesi değil, bu nedenle rolünü düzenleyemezsiniz",
        invalidRole: "{0} Kuruluşa eklenemez veya Kuruluştan çıkarılamaz"
      }
    }
  },
  form: {
    memberList: {
      addUser: "Kullanıcı ekle...",
      create: "Oluştur",
      editUser: "Kullanıcıyı düzenle",
      invitedAs: "({0} olarak davet edildi)"
    }
  },
  notifications: {
    title: "Bildirimler",
    invites: "Davetler",
    invited: "{0} Kuruluşuna katılmaya davet edildiniz",
    inviteAccepted: "{0} için bir daveti kabul ettiniz",
    readAll: "Hepsini okundu olarak işaretle",
    unread: "Okunmamış",
    read: "Okunmuş",
    all: "Tüm Bildirimler",
    invite: {
      all: "Tüm Davetler",
      projects: "Projeler",
      organizations: "Kuruluşlar",
      btns: {
        accept: "Kabul Et",
        decline: "Reddet",
        unaccept: "Kabul Etme işlemini iptal et"
      },
      msgs: {
        accept: "Katılındı: {0}",
        decline: "Katılım reddedildi: {0}",
        unaccept: "Terk edildi: {0}"
      }
    },
    empty: {
      unread: "Okunmamış bildiriminiz yok.",
      read: "Okunmuş bildiriminiz yok.",
      all: "Bildiriminiz yok.",
      invites: "Hiç davetiniz yok"
    },
    project: {
      reviewed: "{0} {1} incelendi ve onaylandı",
      reviewedPartial: "{0} {1} gözden geçirildi ve kısmen onaylandı",
      newVersion: "{0} için yeni bir sürüm yayınlandı: {1}",
      invite: 'You have been invited to join the group {0} on the project {1}',
      inviteRescinded: "{1} projesindeki {0} grubuna davetiniz iptal edildi",
      removed: "{1} projesinde bulunan {0} grubundan çıkarıldınız",
      roleChanged: "{1} projesinde {0} grubuna eklendiniz"
    },
    organization: {
      invite: "{1} kuruluşundaki {0} grubuna katılmaya davet edildiniz",
      inviteRescinded: "{1} kuruluşundaki {0} grubuna davetiniz iptal edildi",
      removed: "{1} kuruluşundaki {0} grubundan çıkarıldınız",
      roleChanged: "{1} kuruluşundaki {0} grubuna eklendiniz"
    }
  },
  visibility: {
    notice: {
      new: "Bu proje yenidir ve bir sürüm yüklenene kadar başkalarına gösterilmeyecektir. Bir sürüm daha uzun süre yüklenmezse proje silinecektir.",
      needsChanges: "Bu proje değişiklik gerektiriyor",
      needsApproval: "Projeyi incelemeye gönderdiniz",
      softDelete: "Proje {0} tarafından silindi"
    },
    name: {
      new: "Yeni",
      public: "Herkese Açık",
      needsChanges: "Değişiklik Gerekiyor",
      needsApproval: "Onay Gerekiyor",
      softDelete: "Geçici Sil"
    },
    changes: {
      version: {
        reviewed: "onaylanan incelemeler nedeniyle"
      }
    },
    modal: {
      activatorBtn: "Görünürlük İşlemleri",
      title: "Görünürlüğünü değiştir: {0}",
      reason: "Değişiklik sebebi",
      success: "Görünürlük değiştirildi: {0}. Yeni görünürlük ayarı: {1}"
    }
  },
  author: {
    watching: "İzleniyor",
    stars: "Yıldızlar",
    orgs: "Kuruluşlar",
    viewOnForums: "Forumlarda görüntüle ",
    taglineLabel: "Kullanıcı Açıklaması",
    editTagline: 'Edit Tagline',
    memberSince: "İlk katılım tarihi: {0}",
    numProjects: "Proje yok | {0} proje | {0} proje",
    addTagline: "Bir açıklama ekle",
    noOrgs: "{0} herhangi bir kuruluşa üye değil. 😢",
    noWatching: "{0} herhangi bir projeyi takip etmiyor. 😢",
    noStarred: "{0} hiçbir projeye yıldız vermedi. 😢",
    tooltips: {
      settings: "Kullanıcı Ayarları",
      lock: "Hesabı Kilitle",
      unlock: "Hesabın Kildini Kaldır",
      apiKeys: "API Anahtarları",
      activity: "Kullanoı Ayarları",
      admin: "Kullanıcı Yönetimi"
    },
    lock: {
      confirmLock: "{0} adlı kişinin hesabı kilitlensin mi?",
      confirmUnlock: "{0} adlı kişinin hesabının kilidi açılsın mı?",
      successLock: "{0} adlı kişinin hesabı başarıyla kilitlendi",
      successUnlock: "{0} adlı kişinin hesabının kilidi başarıyla açıldı"
    },
    org: {
      editAvatar: "Profil fotoğrafını düzenle"
    },
    error: {
      invalidTagline: "Geçersiz slogan",
      invalidUsername: "Geçersiz kullanıcı adı"
    }
  },
  linkout: {
    title: "Harici Bağlantı Uyarısı",
    text: "\"{0}\" için harici bir bağlantıya tıkladınız. Bu bağlantıyı ziyaret etmeyi düşünmediyseniz, lütfen geri dönün. Aksi takdirde, devam tuşuna tıklayın.",
    abort: "Geri dön",
    continue: "Devam"
  },
  flags: {
    header: "Etiketler: ",
    noFlags: "Etiket bulunamadı",
    resolved: "Evet, {1} tarihinde {0} tarafından",
    notResolved: "Hayır"
  },
  notes: {
    header: "Notlar: ",
    noNotes: "Not bulunamadı",
    addNote: "Bir not ekle",
    notes: "Notlar",
    placeholder: "Bir not ekle..."
  },
  stats: {
    title: "İstatikler",
    plugins: "Eklentiler",
    reviews: "İncelemeler",
    uploads: "Yüklemeler",
    downloads: "İndirmeler",
    totalDownloads: "Toplam İndirme",
    unsafeDownloads: "Güvensiz İndirmeler",
    flags: "Etiketler",
    openedFlags: "Mevcut Etiketler",
    closedFlags: "Çözülmüş Etiketler"
  },
  health: {
    title: "Hangar Sağlık Raporu",
    noTopicProject: "Eksik tartışma konusu",
    erroredJobs: "Başarısız işlemler",
    jobText: "İşlem türü: {0}, Hata Türü: {1}, {2} gerçekleşt,",
    staleProjects: "Eski projeler",
    notPublicProjects: "Gizlenmiş projeler",
    noPlatform: "Platform tespit edilemedi",
    missingFileProjects: "Eksik Dosya",
    empty: "Boş! Her şey çok güzel!"
  },
  reviews: {
    headline: "{0} bu sürümü {1} tarihinde yayınladı",
    title: "Günlükleri inceleyin",
    projectPage: "Proje Sayfası",
    downloadFile: "Dosyayı indir",
    startReview: "İncelemeye Başla",
    stopReview: "İncelemeyi Durdur",
    approve: "Onayla",
    approvePartial: "Kısmi Onayla",
    notUnderReview: "Bu sürüm şu anda incelenmiyor.",
    reviewMessage: "Mesajı İncele",
    addMessage: "Mesaj Ekle",
    reopenReview: "İncelemeyi Yeniden Aç",
    undoApproval: "Onayı Geri Al",
    hideClosed: "Tüm bitmiş incelemeleri gizle",
    error: {
      noReviewStarted: "Mesaj eklenecek bitmemiş bir inceleme yok",
      notCorrectUser: "Bu incelemeyi başlatan kullanıcı siz değilsiniz",
      cannotReopen: "Bu inceleme yeniden açılamıyor",
      onlyOneReview: "Bir sürüm için 1'den fazla inceleme olamaz",
      badUndo: "Yalnızca bir onaydan sonra onay geri alınabilir"
    },
    presets: {
      message: '{msg}',
      start: "{name} bir inceleme başlattı",
      stop: "{name} bir incelemeyi durdurdu: {msg}",
      reopen: "{name} bit incelemeyi yeniden açtı",
      approve: "{name} bu versiyonu onayladı",
      approvePartial: "{name} bu versiyonu kısmi olarak onayladı",
      undoApproval: "{name} onayını geri aldı",
      reviewTitle: "{name} isimli kullanıcının incelemesi"
    },
    state: {
      ongoing: "Devam Ediyor",
      stopped: "Durdu",
      approved: "Onaylandı",
      partiallyApproved: "Kısmen Onaylandı",
      lastUpdate: "Son güncelleme: {0}"
    }
  },
  apiKeys: {
    title: "API Anahtarları",
    createNew: "Yeni bir anahtar oluştur",
    existing: "Mevcut anahtarlar",
    name: "Ad",
    key: "Anahtar",
    keyIdentifier: "Anahtar Tanımlayıcı",
    permissions: "Yetkiler",
    delete: "Sil",
    deleteKey: "Bir anahtarı sil",
    createKey: "Bir anahtar oluştur",
    noKeys: "Henüz api anahtarı yok. Sağ tarafta bir tane oluşturabilirsiniz",
    success: {
      delete: "Anahtarı sildiniz: {0}",
      create: 'You have created the key: {0}'
    },
    error: {
      notEnoughPerms: "Anahtar oluşturmak için yeterli izniniz yok",
      duplicateName: "Anahtar adları benzersiz olmalıdır"
    }
  },
  apiDocs: {
    title: "API Dokümentasyonu"
  },
  platformVersions: {
    title: "Platform Sürümlerini Yapılandırın",
    platform: 'Platform',
    versions: "Versiyonlar",
    addVersion: "Versiyon ekle",
    saveChanges: "Değişiklikleri kaydet",
    success: "Güncellenmiş platform sürümleri"
  },
  flagReview: {
    title: "Etiketler",
    noFlags: "İncelenecek bayrak yok.",
    msgUser: "Kullanıcıya mesaj yaz",
    msgProjectOwner: "Proje sahibine mesaj yaz",
    markResolved: "Çözüldü olarak işaretle",
    line1: "{0}, {2} tarihinde bildirdi: {1}",
    line2: "Sebep: {0}",
    line3: "Yorum {0}"
  },
  userActivity: {
    title: "{0} isimli kullanıcının eylemleri",
    reviews: "İncelemeler",
    flags: "Etiketler",
    reviewApproved: "İnceleme Onaylandı",
    flagResolved: "Etiket Çözüldü",
    error: {
      isOrg: "Kuruluş kullanıcıları için etkinlik gösterilemiyor"
    }
  },
  userAdmin: {
    title: "Kullanıcıyı Düzenle",
    organizations: "Kuruluşlar",
    organization: "Kuruluş",
    projects: "Projeler",
    project: "Projeler",
    owner: "Sahip",
    role: "Rol",
    accepted: "Kabul Edildi",
    sidebar: "Yönetim (Diğer)",
    hangarAuth: "HangarAuth Profili",
    forum: "Forum Profili"
  },
  userActionLog: {
    title: "Kullanıcı Eylem Geçmişi",
    user: "Kullanıcı",
    address: "IP Adresi",
    time: "Tarih",
    action: "Eylem",
    context: "Bağlam",
    oldState: "Eski Durum",
    newState: "Yeni Durum",
    markdownView: "Markdown Görünümü",
    diffView: "Fark Görünümü",
    types: {
      ProjectVisibilityChanged: "Proje görünürlük durumu değiştirildi",
      ProjectRename: "Projenin adı değiştirildi",
      ProjectFlagged: "Proje etiketlendi",
      ProjectSettingsChanged: "Proje ayarları değiştirildi",
      ProjectIconChanged: "Proje simgesi değiştirildi",
      ProjectFlagResolved: "Etiket çözüldü",
      ProjectChannelCreated: "Bir proje kanalı oluşturuldu",
      ProjectChannelEdited: "Bir proje kanalı düzenlendi",
      ProjectChannelDeleted: "Bir proje kanalı silindi",
      ProjectInvitesSent: "Proje davetleri gönderildi",
      ProjectInviteDeclined: "Bir proje daveti reddedildi",
      ProjectInviteUnaccepted: "Bir proje davetinin kabul işlemi iptal edildi",
      ProjectMemberAdded: "Bir proje üyesi eklendi",
      ProjectMembersRemoved: "Proje üyeleri kaldırıldı",
      ProjectMemberRolesChanged: "Proje üyelerinin rolleri güncellendi",
      ProjectPageCreated: "Bir proje sayfası oluşturuldu",
      ProjectPageDeleted: "Bir proje sayfası silindi",
      ProjectPageEdited: "Bir proje sayfası düzenlendi",
      VersionVisibilityChanged: "Sürümün görünürlük durumu değiştirildi",
      VersionDeleted: "Sürüm silindi",
      VersionCreated: "Yeni bir sürüm yüklendi",
      VersionDescriptionEdited: "Sürüm açıklaması düzenlendi",
      VersionReviewStateChanged: "Sürümün inceleme durumu değiştirildi",
      VersionPluginDependencyAdded: "Bir eklenti gereksenimi eklendi",
      VersionPluginDependencyEdited: "Bir eklenti gereksinimi düzenlendi",
      VersionPluginDependencyRemoved: "Bir eklenti gereksinimi kaldırıldı",
      VersionPlatformDependencyAdded: "Bir platform gereksinimi eklendi",
      VersionPlatformDependencyRemoved: "Bir platform gereksinimi kaldırıldı",
      UserTaglineChanged: "Kullanıcı sloganı değişti",
      UserLocked: "Bu kullanıcı kilitlendi",
      UserUnlocked: "Bu kullanıcının kilidi kaldırıldı",
      UserApikeyCreated: "Bir API anahtarı oluşturuldu",
      UserApikeyDeleted: "Bir API anahtarı silindi",
      OrganizationInvitesSent: "Kuruluk davetleri gönderildi",
      OrganizationInviteDeclined: "Bir kuruluş daveti reddedildi",
      OrganizationInviteUnaccepted: "Bir kuruluş davet kabulü iptal edildi",
      OrganizationMemberAdded: "Bir kuruluş üyesi eklendi",
      OrganizationMembersRemoved: "Kuruluş üyeleri kaldırıldı",
      OrganizationMemberRolesChanged: "Kuruluş üyelerinin rolleri güncellendi"
    }
  },
  versionApproval: {
    title: "Sürüm Onayları",
    inReview: "İnceleniyor",
    approvalQueue: "Onay kuyruğu",
    queuedBy: "Sıraya alan:",
    status: "Durum",
    project: "Proje",
    date: "Tarih",
    version: "Versiyon",
    started: "Başlandı: {0}",
    ended: "Bitirldi: {0}",
    statuses: {
      ongoing: "{0} devam ediyor",
      stopped: "{0} durdu",
      approved: "{0} onaylandı"
    }
  },
  projectApproval: {
    title: "Proje Onayları",
    sendForApproval: "Projeyi onaya gönderdiniz",
    noProjects: "Proje yok",
    needsApproval: "Onay Bekleniyor",
    awaitingChanges: "Değiklik Bekleniyor",
    description: "{0}, {1} üzerinde değişiklik istedi}"
  },
  donate: {
    title: "{0} için bağış yapın",
    monthly: "Aylık",
    oneTime: "Tek Seferlik",
    selectAmount: "Yukarıdan bir miktar seçin veya aşağıya bir miktar girin",
    legal: "{0} için bağışta bulunarak Y'yi ve tacoların lezzetli olabileceğini kabul etmiş olursunuz",
    cta: "Bağış Yap",
    submit: "{0} bağışla"
  },
  lang: {
    button: "Dil Değiştir",
    title: "Dil Değiştir",
    available: "Mevcut Dil",
    hangarAuth: "Bu, yalnızca mevcut tarayıcınızın yerel ayarını değiştirir (çerez olarak). Tüm Paper hizmetleri için Paper yetkilendirme dilinizi değiştirmek için burayı tıklayın"
  },
  validation: {
    required: "{0} bulunmak zorunda",
    maxLength: "Maksimum uzunluk {0}",
    minLength: "Minimum uzunluk {0}",
    invalidFormat: "{0} geçersiz",
    invalidUrl: "Geçersiz URL formatı"
  },
  prompts: {
    confirm: "Anladım!",
    changeAvatar: {
      title: "Avatarınızı değiştirin!",
      message: "Yeni Kuruluşunuza hoş geldiniz! İşe üzerine tıklayarak avatarını değiştirerek başlayın."
    }
  },
  error: {
    userLocked: "Hesabınız kilitli",
    401: "Bunun için giriş yapmalısınız",
    403: "Bunu yapmaya iznin yok",
    404: "404 sayfa bulunamadı",
    unknown: "Bir hata gerçekleşti"
  }
};
export default msgs;
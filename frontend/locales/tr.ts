import { LocaleMessageObject } from 'vue-i18n';

const msgs: LocaleMessageObject = {
    general: {
        close: 'Kapat',
        submit: 'Gönder',
        save: 'Kaydet',
        comment: 'Yorum yap',
        change: 'Değiştir',
        donate: 'Bağışta bulun',
        continue: 'Devam et',
        create: 'Oluştur',
        delete: 'Sil',
        or: 'Veya',
        reset: 'Sıfırla',
        edit: 'Düzenle',
        required: 'Gerekli',
        add: 'Ekle',
        name: 'Ad',
        link: 'Bağlantı',
        send: 'Gönder',
        home: 'Anasayfa',
        message: 'Mesaj Gönder',
        refresh: 'Yenile',
        confirm: 'Doğrula',
        error: {
            invalidUrl: 'Geçersiz URL formatı',
        },
    },
    hangar: {
        projectSearch: {
            query: 'Topluluk tarafından gururla tasarlanmış {0} proje içinde ara...',
            relevanceSort: 'Alaka düzeyine göre sırala',
            noProjects: 'Proje yok. 😢',
            noProjectsFound: '0 proje bulundu. 😢',
        },
        subtitle: 'Bir Minecraft paket deposu.',
        sponsoredBy: 'Sponsor: ',
    },
    pages: {
        staffTitle: 'Personel',
        authorsTitle: 'Yazarlar',
        headers: {
            username: 'Kullanıcı adı',
            roles: 'Roller',
            joined: 'Katıldı',
            projects: 'Projeler',
        },
    },
    nav: {
        login: 'Giriş yap',
        signup: 'Kayıt yap',
        user: {
            notifications: 'Bildirimler',
            flags: 'Etiketler',
            projectApprovals: 'Proje onayları',
            versionApprovals: 'Versiyon onayları',
            stats: 'İstatikler',
            health: 'Hangar Sağlığı',
            log: 'Kullanıcı eylem geçmişi',
            platformVersions: 'Platform Versiyonları',
            logout: 'Çıkış yap',
            error: {
                loginFailed: 'Kimlik doğrulama Başarısız',
                invalidUsername: 'Geçersiz Kullanıcı adı',
                hangarAuth: "HangarAuth'a bağlanılamadı",
                loginDisabled: 'Giriş geçici olarak kullanılamıyor, lütfen daha sonra tekrar deneyin',
                fakeUserEnabled: 'Sahte kullanıcı etkinleştirildi. {0} bu nedenle devre dışı bırakıldı',
            },
        },
        createNew: 'Yeni oluştur...',
        new: {
            project: 'Yeni Proje',
            organization: 'Yeni Organizasyon',
        },
        hangar: {
            home: 'Anasayfa',
            forums: 'Forumlar',
            code: 'Kod',
            docs: 'Dokümentasyon',
            javadocs: 'JavaDocları',
            hangar: 'Hangar (Pluginler)',
            downloads: 'İndirmeler',
            community: 'Topluluk',
        },
    },
    project: {
        stargazers: 'Yıldız gözlemcileri',
        noStargazers: 'Bu projenin hiç yıldız gözlemcisi yok 😢',
        watchers: 'İzleyenler',
        noWatchers: 'Bu projeyi izleyen yok 😢',
        members: 'Üyeler',
        category: {
            info: 'Kategori: {0}',
            admin_tools: 'Yönetici Araçları',
            chat: 'Sohbet',
            dev_tools: 'Geliştirici Araçları',
            economy: 'Ekonomi',
            gameplay: 'Oynanış',
            games: 'Oyunlar',
            protection: 'Koruma',
            role_playing: 'Rol Yapma',
            world_management: 'Dünya Yönetimi',
            misc: 'Çeşitli',
        },
        actions: {
            unwatch: 'İzlemeyi bırak',
            watch: 'İzle',
            flag: 'Etiketle',
            star: 'Yıldızla',
            unstar: 'Yıldızı kaldır',
            adminActions: 'Yönetici seçenekleri',
            flagHistory: 'Etiket geçmişi ({0})',
            staffNotes: 'Personel notları ({0})',
            userActionLogs: 'Kullanıcı eylem geçmişi',
            forum: 'Forum',
        },
        flag: {
            flagProject: '{0} etiketlensin mi?',
            flagSend: 'Başarıyla etiketlendi, bu topluluğu daha iyi bir yer yaptığınız için teşekkürler!',
            flagSent: 'Etiket inceleme için gönderildi',
            flags: {
                inappropriateContent: 'Uygunsuz İçerik',
                impersonation: 'Taklit etme veya Aldatma',
                spam: 'Spam (Aşırı tekrar eden içerik)',
                malIntent: 'Kötü Amaç',
                other: 'Diğer',
            },
            error: {
                alreadyOpen: 'You can only have 1 unresolved flag on a project',
                alreadyResolved: 'This flag is already resolved',
            },
        },
        tabs: {
            docs: 'Dokümentasyon',
            versions: 'Versiyonlar',
            discuss: 'Tartış',
            settings: 'Ayarlar',
            homepage: 'Anasayfa',
            issues: 'Sorunlar',
            source: 'Kaynak',
            support: 'Destek',
        },
        new: {
            step1: {
                title: 'Kullanıcı Anlaşması',
                text: 'Bir proje, sizin indirmelerinizi ve sizin eklentinizin belgelerini içerir.<br>Devam etmeden önce lütfen <a href="#">Hangar Gönderim Yönergelerine.</a> bir göz atın.',
                continue: 'Kabul ediyorum',
                back: 'İptal',
            },
            step2: {
                title: 'Basit Ayarlar',
                continue: 'Devam',
                back: 'Geri',
                userSelect: 'Kullanıcı seç',
                projectName: 'Proje Adı',
                projectSummary: 'Proje Özeti',
                projectCategory: 'Proje Kategorisi',
            },
            step3: {
                title: 'Ek Ayarlar',
                continue: 'Devam',
                back: 'Geri',
                optional: 'İsteğe Bağlı',
                links: 'Bağlantılar',
                homepage: 'Anasayfa',
                issues: 'Sorun İzleyici',
                source: 'Kaynak Kodu',
                support: 'Harici Destek',
                license: 'Lisans',
                type: 'Tip',
                customName: 'Ad',
                url: 'URL',
                seo: 'SEO',
                keywords: 'Anahtar Kelimeler',
            },
            step4: {
                title: 'Spigottan İçe aktar',
                continue: 'Devam',
                back: 'Geri',
                optional: 'İsteğe Bağlı',
                convert: 'Dönüştür',
                saveAsHomePage: 'Ana Sayfa olarak kaydet',
                convertLabels: {
                    bbCode: 'BBKodunu buraya yapıştır',
                    output: 'Markdown Çıktısı',
                },
                preview: 'Önizleme',
                tutorial: 'BBKod nasıl alınır',
                tutorialInstructions: {
                    line1: 'Spigot projenizin BBKodunu almak için şunları yapın:',
                    line2: '1. Projenize gidin ve "Edit Resource (Kaynağı Düzenle)" yazan yere basın.',
                    line3: '2. Açıklama düzenleyicide İngiliz anahtarı sembolüne tıklayın.',
                    line4: '3. Kopyala yeni içeriği üst dönüştürücü metin kutusuna yapıştırın, isterseniz çıktıda değişiklik yapın ve kaydete basın!',
                },
            },
            step5: {
                title: 'Bitiriliyor',
                text: 'Oluşturuluyor...',
            },
            error: {
                create: 'Proje oluşturulurken bir hata gerçekleşti',
                nameExists: 'Bu isimde bir proje zaten var',
                slugExists: 'Bu harf dizisi ile bir proje zaten var',
                invalidName: 'Bu isim geçersiz karakterler içeriyor',
                tooLongName: 'Proje adı çok uzun',
                tooLongDesc: 'Proje açıklaması çok uzun',
                tooManyKeywords: 'Proje çok fazla anahtar kelime içeriyor',
                noCategory: 'Proje bir kategoriye dahil olmalıdır',
                noDescription: 'Projenin bir açıklaması olmalıdır',
            },
        },
        sendForApproval: 'Onay için gönder',
        info: {
            title: 'Detaylar',
            publishDate: '{0} tarihinde paylaşıldı',
            views: '0 görüntüleme | {0} görüntüleme | {0} görüntüleme',
            totalDownloads: '0 toplam indirme | {0} toplam indirme | {0} toplam indirme',
            stars: '0 yıldız | {0} yıldız | {0} yıldız',
            watchers: '0 izleyici | {0} izleyici | {0} izleyici',
        },
        promotedVersions: 'Tanıtılan Sürümler',
        license: {
            link: 'Lisans: ',
        },
        error: {
            star: 'Yıldızlanmışlar değiştirilemedi',
            watch: 'İzlenenler değiştirilemedi',
        },
        settings: {
            title: 'Ayarlar',
            category: 'Kategori',
            categorySub: 'Projenizi 10 kategoriden birine ayırın. Projenizi uygun şekilde kategorize etmek, insanların bulmasını kolaylaştırır.',
            keywords: 'Anahtar kelimeler',
            keywordsSub: 'Bunlar, insanlar onları aramalarına eklediğinde insanları projenize yönlendirecek özel kelimelerdir.',
            homepage: 'Anasayfa',
            homepageSub:
                'Projeniz için özel bir ana sayfaya sahip olmak, daha düzgün, resmi görünmenize yardımcı olur ve projeniz hakkında bilgi toplamak için size başka bir yer sağlar.',
            issues: 'Issue tracker',
            issuesSub: 'Providing an issue tracker helps your users get support more easily and provides you with an easy way to track bugs.',
            source: 'Kaynak Kodu',
            sourceSub: 'Projenizi açık kaynak yaparak geliştiriciler topluluğunu destekleyin!',
            support: 'Harici Destek',
            supportSub: 'Kullanıcılarınıza destek sunabileceğiniz harici bir yer. Bir forum, Discord sunucusu veya başka bir yer olabilir.',
            license: 'Lisans',
            licenseSub: 'İnsanlar projenizi nasıl kullanabilirler? (Ve nasıl kullanamazlar.)',
            forum: 'Forumda gönderi oluşturun',
            forumSub: 'Yeni bir sürüm gibi durumlarda forumlarda otomatik olarak bir gönderi oluşturup oluşturmayacağını ayarlar',
            description: 'Açıklama',
            descriptionSub: 'Projeniz hakkında kısa bir açıklama.',
            icon: 'Simge',
            iconSub: 'Projenizi temsil eden bir resim yükleyin',
            iconUpload: 'Yükle',
            iconReset: 'Simgeyi',
            apiKey: 'API Anahtarları',
            apiKeySub: 'Gradle ile derleme dağıtımını etkinleştirmek için benzersiz bir dağıtım anahtarı oluşturun',
            apiKeyGenerate: 'Oluştur',
            rename: 'Yeniden adlandır',
            renameSub: 'Projenizin adını değiştirmek istenmeyen sonuçlara yol açabilir. Herhangi bir yeniden yönlendirme ayarlamayacağız.',
            delete: 'Sil',
            deleteSub: 'Bir projeyi sildikten sonra geri alınamaz.',
            hardDelete: 'Kesin Silme',
            hardDeleteSub: 'Bir projeyi sildikten sonra geri alınamaz. Bu sefer gerçekten alınamaz...',
            save: 'Değişiklikleri kaydet',
            optional: '(isteğe bağlı)',
            licenseCustom: 'Ad',
            licenseType: 'Tip',
            licenseUrl: 'URL',
            donation: {
                enable: 'Etkinleştir',
                enableSub: 'Bu proje için bağış formunu etkinleştirin',
                email: 'E-Posta',
                emailSub: 'Bağışları alması gereken paypal hesabının E-Posta adresi',
                defaultAmount: 'Varsayılan Tutar',
                defaultAmountSub: 'Önceden seçilmiş varsayılan bağış miktarı',
                oneTimeAmounts: 'Tek Seferlik Tutarlar',
                oneTimeAmountsSub: 'Kullanıcılara tek seferlik bağışlar için vermek istediğiniz seçeneklerin listesi. Kullanıcılar her zaman özel bir tutar girebilir',
                monthlyAmounts: 'Aylık Tutarlar',
                monthlyAmountsSub: 'Aylık bağışlar için kullanıcılara sunmak istediğiniz seçeneklerin listesi. Kullanıcılar her zaman özel bir tutar girebilir',
            },
            error: {
                invalidFile: '{0} geçersiz bir dosya tipi',
                noFile: 'Dosya yüklenmedi',
                members: {
                    invalidUser: '{0} geçerli bir kullanıcı değil',
                    alreadyInvited: '{0} zaten bu projeye davet edildi',
                    notMember: '{0} projenin bir üyesi değil, bu nedenle rollerini düzenleyemezsiniz',
                    invalidRole: '{0} bu projeye eklenemez ve bu projeden kaldırılamaz',
                },
            },
            success: {
                changedIcon: 'Proje simgesi başarıyla değiştirildi',
                resetIcon: 'Proje simgesi başarıyla sıfırlandı',
                rename: 'Projenin adı {0} olarak değiştirildi',
                softDelete: 'Bu projeyi sildiniz',
                hardDelete: 'Bu projeyi tamamen sildiniz',
            },
            tabs: {
                general: 'Genel',
                optional: 'İsteğe Başlı',
                management: 'Yönetim',
                donation: 'Bağış',
            },
        },
        discuss: {
            login: 'Giriş yap',
            toReply: 'bu tartışmaya cevap vermek için',
            noTopic: 'Bu proje için bir tartışma yok',
            send: 'Cevap gönderildi!',
        },
    },
    page: {
        plural: 'Sayfalar',
        new: {
            title: 'Yeni bir sayfa oluştur',
            error: {
                minLength: 'Sayfa içerikleri çok kısa',
                maxLength: 'Sayfa içerikleri çok uzun',
                duplicateName: 'Bu ad ile bir sayfa zaten var',
                invalidName: 'Geçersiz isim',
                name: {
                    maxLength: 'Sayfa adı çok kısa',
                    minLength: 'Sayfa adı çok uzun',
                    invalidChars: 'Sayfa adı geçersiz karakterler içeriyor',
                },
                save: 'Sayfa kaydedilemedi',
            },
            name: 'Sayfa Adı',
            parent: 'Ana Sayfa (isteğe bağlı)',
        },
        delete: {
            title: 'Sayfa silisin mi?',
            text: 'Sayfa silisin mi? Bu işlem geri alınamaz!',
        },
    },
    version: {
        new: {
            title: 'Versiyon oluştur...',
            upload: 'Dosya Yükle',
            uploadNew: 'Yeni bir Versiyon Yükle',
            url: 'Bir URL girin',
            form: {
                versionString: 'Versiyon',
                fileName: 'Dosya adı',
                fileSize: 'Dosya boyutu',
                externalUrl: 'Harici URL',
                hangarProject: 'Hangar Projesi',
                channel: 'Kanal',
                addChannel: 'Kanal Ekle',
                unstable: 'Dengesiz',
                recommended: 'Önerilen',
                forumPost: 'Forum Gönderisi',
                release: {
                    bulletin: 'Yayın Bülteni',
                    desc: "Bu sürümdeki yenilikler neler?",
                },
                platforms: 'Platforlar',
                dependencies: 'Eklenti Gereksinimleri',
            },
            error: {
                metaNotFound: 'Yüklenen dosyadan metaveriler yüklenemedi',
                jarNotFound: 'Jar dosyası açılamadı',
                fileExtension: 'Hatalı dosya uzantısı',
                unexpected: 'Beklenmedik bir hata gerçekleşti',
                invalidVersionString: 'Geçersiz sürüm dizesi bulundu',
                duplicateNameAndPlatform: 'Bu ada ve uyumlu platforma sahip bir sürüm zaten var',
                invalidNumOfPlatforms: 'Geçersiz sayıda platform',
                duplicate: 'Bu dosyaya sahip bir sürüm zaten var',
                noFile: 'Yüklenen dosya bulunamadı',
                mismatchedFileSize: 'Dosya boyutları uyuşmadı',
                hashMismatch: 'Dosya karma değerleri (hash) eşleşmiyor',
                invalidPlatformVersion: 'Belirtilen bir platform için geçersiz MC sürümü',
                fileIOError: 'Dosya IO Hatası',
                unknown: 'Bilinmeyen bir hata gerçekleşti',
                incomplete: 'Plugin dosyasında {0} eksik',
                noDescription: 'Bir açıklama bulunmalı',
                invalidPluginDependencyNamespace: 'Bildirilen eklenti gereksinimi geçersiz bir proje ad alanına (namespace) sahip',
                invalidName: 'Geçersiz versiyon adı',
                channel: {
                    noName: 'Bir kanal adı  belirtilmelidir',
                    noColor: 'Bir kanal rengi belirtilmelidir',
                },
            },
        },
        edit: {
            platformVersions: 'Platform Sürümlerini Düzenle: {0}',
            pluginDeps: 'Eklenti Gereksinimlerini Düzenle: {0}',
            error: {
                noPlatformVersions: 'En az bir geçerli platform sürümü sağlanmalıdır',
                invalidVersionForPlatform: '{0}, {1} için geçersiz bir sürüm',
                invalidProjectNamespace: '{0} geçerli bir proje ad alanı (namespace) değil',
            },
        },
        page: {
            subheader: '{0}, bu sürümü {1} tarihinde yayınladı',
            dependencies: 'Gereksinimler',
            platform: 'Platform',
            required: '(zorunlu)',
            adminMsg: '{0}, bu sürümü {1} tarihinde onayladı',
            reviewLogs: 'Review logs',
            reviewStart: 'İnceleme Başlat',
            setRecommended: 'Önerilen olarak ayarla',
            setRecommendedTooltip: 'Bu sürümü {0} platformu için önerilen şekilde ayarlayın',
            delete: 'Sil',
            hardDelete: 'Sil (sonsuza dek)',
            restore: 'Geri getir',
            download: 'İndir',
            downloadExternal: 'Harici İndir',
            adminActions: 'Yönetici eylemleri',
            recommended: 'Önerilen versiyon',
            partiallyApproved: 'Kısmen onaylandı',
            approved: 'Onaylandı',
            userAdminLogs: 'Kullanıcı Yönetici Günlükleri',
            unsafeWarning: 'Bu sürüm, denetleme ekibimiz tarafından incelenmemiştir ve indirilmesi güvenli olmayabilir.',
            downloadUrlCopied: 'Kopyalandı!',
            confirmation: {
                title: 'Uyarı - {0} {2} tarafından {1}',
                alert: 'Bu sürüm, denetleme ekibimiz tarafından henüz incelenmedi ve kullanımı güvenli olmayabilir.',
                disclaimer: 'Sorumluluk Reddi: Bu uyarıyı dikkate almamayı seçerseniz, sunucunuza veya sisteminize gelebilecek herhangi bir zararın sorumluluğunu reddediyoruz..',
                agree: 'Sorumluluğu kabul editorum, indir',
                deny: 'Geri git',
            },
        },
        channels: 'Kanallar',
        editChannels: 'Kanalları Düzenle',
        platforms: 'Platformlar',
        error: {
            onlyOnePublic: 'Yalnızca 1 genel sürümünüz kaldı',
        },
        success: {
            softDelete: 'Bu versiyonu sildiniz.',
            hardDelete: 'Bu versiyonu tamamen sildiniz',
            restore: 'Bu versiyonu geri getirdiniz',
            recommended: 'Bu sürümü {0} platformu için önerilen olarak işaretlediniz',
        },
    },
    channel: {
        modal: {
            titleNew: 'Yeni bir kanal ekle',
            titleEdit: 'Kanalı düzenle',
            name: 'Kanal Adı',
            color: 'Kanal Rengi',
            reviewQueue: 'Denetleme inceleme kuyruğundan hariç tutulsun mu?',
            error: {
                invalidName: 'Geçersiz kanal adı',
                maxChannels: 'Bu proje zaten maksimum kanal sayısına sahip: {0}',
                duplicateColor: 'Bu proje zaten bu renkte bir kanala sahip',
                duplicateName: 'Bu projede zaten bu ada sahip bir kanal var',
                tooLongName: 'Kanal adınız çok uzun',
                cannotDelete: 'Bu kanalı silemezsiniz',
            },
        },
        manage: {
            title: 'Yayın kanalları',
            subtitle: 'Sürüm kanalları, bir eklenti sürümünün durumunu temsil eder. Bir projede en fazla beş yayın kanalı olabilir.',
            channelName: 'Kanal Adı',
            versionCount: 'Sürüm Sayısı',
            reviewed: 'İncelendi',
            edit: 'Düzenle',
            trash: 'Çöp',
            editButton: 'Düzenle',
            deleteButton: 'Sil',
            add: 'Kanal Ekle',
        },
    },
    organization: {
        new: {
            title: 'Bir Kuruluş kur',
            text: "Kuruluşlar, Hangar'daki projeleriniz dahilinde kullanıcıları gruplamanıza ve aralarında daha yakın işbirliği sağlamanıza olanak tanır.", /*RETURN HERE LATER*/
            name: 'Organizasyon adı',
            error: {
                duplicateName: 'Bu ada sahip bir Kuruluş/kullanıcı zaten var',
                invalidName: 'Geçersiz Kuruluş adı.',
                tooManyOrgs: 'En fazla {0} Kuruluş oluşturabilirsiniz',
                notEnabled: 'Kuruluşlar etkin değil!',
                jsonError: "HangarAuth'tan JSON yanıtı ayrıştırılırken hata oluştu",
                hangarAuthValidationError: 'Doğrulama Hatası: {0}',
                unknownError: 'Organizasyon oluşturulurken bilinmeyen hata',
            },
        },
        settings: {
            members: {
                invalidUser: '{0} geçerli bir kullanıcı değil',
                alreadyInvited: '{0} zaten Kuruluşa davet edildi',
                notMember: '{0} Kuruluşun bir üyesi değil, bu nedenle rolünü düzenleyemezsiniz',
                invalidRole: '{0} Kuruluşa eklenemez veya Kuruluştan çıkarılamaz',
            },
        },
    },
    form: {
        memberList: {
            addUser: 'Kullanıcı ekle...',
            create: 'Oluştur',
            editUser: 'Kullanıcıyı düzenle',
            invitedAs: '({0} olarak davet edildi)',
        },
    },
    notifications: {
        title: 'Bildirimler',
        invites: 'Davetler',
        invited: '{0} Kuruluşuna katılmaya davet edildiniz',
        inviteAccepted: '{0} için bir daveti kabul ettiniz',
        readAll: 'Hepsini okundu olarak işaretle',
        unread: 'Okunmamış',
        read: 'Okunmuş',
        all: 'Tüm Bildirimler',
        invite: {
            all: 'Tüm Davetler',
            projects: 'Projeler',
            organizations: 'Kuruluşlar',
            btns: {
                accept: 'Kabul Et',
                decline: 'Reddet',
                unaccept: 'Kabul Etme işlemini iptal et',
            },
            msgs: {
                accept: 'Katılındı: {0}',
                decline: 'Katılım reddedildi: {0}',
                unaccept: 'Terk edildi: {0}',
            },
        },
        empty: {
            unread: 'Okunmamış bildiriminiz yok.',
            read: 'Okunmuş bildiriminiz yok.',
            all: 'Bildiriminiz yok.',
            invites: 'Hiç davetiniz yok',
        },
        project: {
            reviewed: '{0} {1} incelendi ve onaylandı',
            reviewedPartial: '{0} {1} has been reviewed and is partially approved',
            newVersion: 'A new version has been released for {0}: {1}',
            invite: 'You have been invited to join the group {0} on the project {1}',
            inviteRescinded: 'Your invite to you the group {0} in the project {1} has been rescinded',
            removed: 'You have been removed from the group {0} in the project {1}',
            roleChanged: 'You have been added to the {0} group in the project {1}',
        },
        organization: {
            invite: 'You have been invited to join the group {0} in the organization {1}',
            inviteRescinded: 'Your invite to you the group {0} in the organization {1} has been rescinded',
            removed: 'You have been removed from the group {0} in the organization {1}',
            roleChanged: 'You have been added to the {0} group in the organization {1}',
        },
    },
    visibility: {
        notice: {
            new: 'This project is new, and will not be shown to others until a version has been uploaded. If a version is not uploaded over a longer time the project will be deleted.',
            needsChanges: 'This project requires changes',
            needsApproval: 'You have sent the project for review',
            softDelete: 'Project deleted by {0}',
        },
        name: {
            new: 'New',
            public: 'Public',
            needsChanges: 'Needs Changes',
            needsApproval: 'Needs Approval',
            softDelete: 'Soft Delete',
        },
        changes: {
            version: {
                reviewed: 'due to approved reviews',
            },
        },
        modal: {
            activatorBtn: 'Visibility Actions',
            title: "Change {0}'s visibility",
            reason: 'Reason for change',
            success: "You changed the {0}'s visibility to {1}",
        },
    },
    author: {
        watching: 'Watching',
        stars: 'Stars',
        orgs: 'Organizations',
        viewOnForums: 'View on forums ',
        taglineLabel: 'User Tagline',
        editTagline: 'Edit Tagline',
        memberSince: 'A member since {0}',
        numProjects: 'No projects | {0} project | {0} projects',
        addTagline: 'Add a tagline',
        noOrgs: '{0} is not part of any organizations. 😢',
        noWatching: '{0} is not watching any projects. 😢',
        noStarred: '{0}  has not starred any projects. 😢',
        tooltips: {
            settings: 'User Settings',
            lock: 'Lock Account',
            unlock: 'Unlock Account',
            apiKeys: 'API Keys',
            activity: 'User Activity',
            admin: 'User Admin',
        },
        lock: {
            confirmLock: "Lock {0}'s account?",
            confirmUnlock: "Unlock {0}'s account?",
            successLock: "Successfully locked {0}'s account",
            successUnlock: "Successfully unlocked {0}'s account",
        },
        org: {
            editAvatar: 'Edit avatar',
        },
        error: {
            invalidTagline: 'Invalid tagline',
            invalidUsername: 'Invalid username',
        },
    },
    linkout: {
        title: 'External Link Warning',
        text: 'You have clicked on an external link to "{0}". If you did not intend to visit this link, please go back. Otherwise, click continue.',
        abort: 'Go Back',
        continue: 'Continue',
    },
    flags: {
        header: 'Flags for',
        noFlags: 'No flags found',
        resolved: 'Yes, by {0} on {1}',
        notResolved: 'No',
    },
    notes: {
        header: 'Notes for',
        noNotes: 'No notes found',
        addNote: 'Add note',
        notes: 'Notes',
        placeholder: 'Add a note...',
    },
    stats: {
        title: 'Stats',
        plugins: 'Plugins',
        reviews: 'Reviews',
        uploads: 'Uploads',
        downloads: 'Downloads',
        totalDownloads: 'Total Downloads',
        unsafeDownloads: 'Unsafe Downloads',
        flags: 'Flags',
        openedFlags: 'Opened Flags',
        closedFlags: 'Closed Flags',
    },
    health: {
        title: 'Hangar Health Report',
        noTopicProject: 'Missing discussion topic',
        erroredJobs: 'Failed jobs',
        jobText: 'Job type: {0}, Error Type: {1}, Happened: {2}',
        staleProjects: 'Stale projects',
        notPublicProjects: 'Hidden projects',
        noPlatform: 'No platform detected',
        missingFileProjects: 'Missing File',
        empty: 'Empty! All good!',
    },
    reviews: {
        headline: '{0} released this version on {1}',
        title: 'Review logs',
        projectPage: 'Project Page',
        downloadFile: 'Download File',
        startReview: 'Start Review',
        stopReview: 'Stop Review',
        approve: 'Approve',
        approvePartial: 'Approve Partial',
        notUnderReview: 'This version is not under review',
        reviewMessage: 'Review Message',
        addMessage: 'Add Message',
        reopenReview: 'Reopen Review',
        undoApproval: 'Undo Approval',
        hideClosed: 'Hide all finished reviews',
        error: {
            noReviewStarted: 'There is no unfinished review to add a message to',
            notCorrectUser: 'You are not the user that started this review',
            cannotReopen: 'Unable to reopen this review',
            onlyOneReview: 'Cannot have more than 1 review for a version',
            badUndo: 'Can only undo approval after an approval',
        },
        presets: {
            message: '{msg}',
            start: '{name} started a review',
            stop: '{name} stopped a review: {msg}',
            reopen: '{name} reopened a review',
            approve: '{name} approved this version',
            approvePartial: '{name} partially approved this version',
            undoApproval: '{name} has undone their approval',
            reviewTitle: "{name}'s Review",
        },
        state: {
            ongoing: 'Ongoing',
            stopped: 'Stopped',
            approved: 'Approved',
            partiallyApproved: 'Partially Approved',
            lastUpdate: 'Last Update: {0}',
        },
    },
    apiKeys: {
        title: 'API Keys',
        createNew: 'Create new key',
        existing: 'Existing keys',
        name: 'Name',
        key: 'Key',
        keyIdentifier: 'Key Identifier',
        permissions: 'Permissions',
        delete: 'Delete',
        deleteKey: 'Delete Key',
        createKey: 'Create key',
        noKeys: 'There are no api keys yet. You can create one on the right side',
        success: {
            delete: 'You have deleted the key: {0}',
            create: 'You have created the key: {0}',
        },
        error: {
            notEnoughPerms: 'Not enough permissions to create that key',
            duplicateName: 'Duplicate key name',
        },
    },
    apiDocs: {
        title: 'API Docs',
    },
    platformVersions: {
        title: 'Configure Platform Versions',
        platform: 'Platform',
        versions: 'Versions',
        addVersion: 'Versiyon ekle',
        saveChanges: 'Değişiklikleri kaydet',
        success: 'Güncellenmiş platform sürümleri',
    },
    flagReview: {
        title: 'Flags',
        noFlags: 'İncelenecek bayrak yok.',
        msgUser: 'Kullanıcıya mesaj yaz',
        msgProjectOwner: 'Proje sahibine mesaj yaz',
        markResolved: 'Çözüldü olarak işaretle',
        line1: '{0}, {2} tarihinde bildirdi: {1}',
        line2: 'Sebep: {0}',
        line3: 'Yorum {0}',
    },
    userActivity: {
        title: "{0} isimli kullanıcının eylemleri",
        reviews: 'İncelemeler',
        flags: 'Etiketler',
        reviewApproved: 'İnceleme Onaylandı',
        flagResolved: 'Etiket Çözüldü',
        error: {
            isOrg: 'Kuruluş kullanıcıları için etkinlik gösterilemiyor',
        },
    },
    userAdmin: {
        title: 'Kullanıcıyı Düzenle',
        organizations: 'Kuruluşlar',
        organization: 'Kuruluş',
        projects: 'Projeler',
        project: 'Projeler',
        owner: 'Sahip',
        role: 'Rol',
        accepted: 'Kabul Edildi',
        sidebar: 'Yönetim (Diğer)',
        hangarAuth: 'HangarAuth Profili',
        forum: 'Forum Profili',
    },
    userActionLog: {
        title: 'Kullanıcı Eylem Geçmişi',
        user: 'Kullanıcı',
        address: 'IP Adresi',
        time: 'Tarih',
        action: 'Eylem',
        context: 'Bağlam',
        oldState: 'Eski Durum',
        newState: 'Yeni Durum',
        markdownView: 'Markdown Görünümü',
        diffView: 'Fark Görünümü',
        types: {
            ProjectVisibilityChanged: 'Proje görünürlük durumu değiştirildi',
            ProjectRename: 'Projenin adı değiştirildi',
            ProjectFlagged: 'Proje etiketlendi',
            ProjectSettingsChanged: 'Proje ayarları değiştirildi',
            ProjectIconChanged: 'Proje simgesi değiştirildi',
            ProjectFlagResolved: 'Etiket çözüldü',
            ProjectChannelCreated: 'Bir proje kanalı oluşturuldu',
            ProjectChannelEdited: 'Bir proje kanalı düzenlendi',
            ProjectChannelDeleted: 'Bir proje kanalı silindi',
            ProjectInvitesSent: 'Proje davetleri gönderildi',
            ProjectInviteDeclined: 'Bir proje daveti reddedildi',
            ProjectInviteUnaccepted: 'Bir proje davetinin kabul işlemi iptal edildi',
            ProjectMemberAdded: 'Bir proje üyesi eklendi',
            ProjectMembersRemoved: 'Proje üyeleri kaldırıldı',
            ProjectMemberRolesChanged: 'Proje üyelerinin rolleri güncellendi',
            ProjectPageCreated: 'Bir proje sayfası oluşturuldu',
            ProjectPageDeleted: 'Bir proje sayfası silindi',
            ProjectPageEdited: 'Bir proje sayfası düzenlendi',
            VersionVisibilityChanged: 'Sürümün görünürlük durumu değiştirildi',
            VersionDeleted: 'Sürüm silindi',
            VersionCreated: 'Yeni bir sürüm yüklendi',
            VersionDescriptionEdited: 'Sürüm açıklaması düzenlendi',
            VersionReviewStateChanged: 'Sürümün inceleme durumu değiştirildi',
            VersionPluginDependencyAdded: 'Bir eklenti gereksenimi eklendi',
            VersionPluginDependencyEdited: 'Bir eklenti gereksinimi düzenlendi',
            VersionPluginDependencyRemoved: 'Bir eklenti gereksinimi kaldırıldı',
            VersionPlatformDependencyAdded: 'Bir platform gereksinimi eklendi',
            VersionPlatformDependencyRemoved: 'Bir platform gereksinimi kaldırıldı',
            UserTaglineChanged: 'Kullanıcı sloganı değişti',
            UserLocked: 'Bu kullanıcı kilitlendi',
            UserUnlocked: 'Bu kullanıcının kilidi kaldırıldı',
            UserApikeyCreated: 'Bir API anahtarı oluşturuldu',
            UserApikeyDeleted: 'Bir API anahtarı silindi',
            OrganizationInvitesSent: 'Kuruluk davetleri gönderildi',
            OrganizationInviteDeclined: 'Bir kuruluş daveti reddedildi',
            OrganizationInviteUnaccepted: 'Bir kuruluş davet kabulü iptal edildi',
            OrganizationMemberAdded: 'An organization member was added',
            OrganizationMembersRemoved: 'Kuruluş üyeleri kaldırıldı',
            OrganizationMemberRolesChanged: 'Kuruluş üyelerinin rolleri güncellendi',
        },
    },
    versionApproval: {
        title: 'Sürüm Onayları',
        inReview: 'İnceleniyor',
        approvalQueue: 'Onay kuyruğu',
        queuedBy: 'Sıraya alan:',
        status: 'Durum',
        project: 'Proje',
        date: 'Tarih',
        version: 'Versiyon',
        started: 'Başlandı: {0}',
        ended: 'Bitirldi: {0}',
        statuses: {
            ongoing: '{0} devam ediyor',
            stopped: '{0} durdu',
            approved: '{0} onaylandı',
        },
    },
    projectApproval: {
        title: 'Proje Onayları',
        sendForApproval: 'Projeyi onaya gönderdiniz',
        noProjects: 'No projects',
        needsApproval: 'Needs Approval',
        awaitingChanges: 'Awaiting Changes',
        description: '{0}, {1} üzerinde değişiklik istedi}',
    },
    donate: {
        title: '{} için bağış yapın',
        monthly: 'Aylık',
        oneTime: 'Tek Seferlik',
        selectAmount: 'Yukarıdan bir miktar seçin veya aşağıya bir miktar girin',
        legal: "{0} için bağışta bulunarak Y'yi ve tacoların lezzetli olabileceğini kabul etmiş olursunuz",
        cta: 'Bağış Yap',
        submit: '{0} bağışla',
    },
    lang: {
        button: 'Dil Değiştir',
        title: 'Dil Değiştir',
        available: 'Mevcut Dil',
        hangarAuth: 'Bu, yalnızca mevcut tarayıcınızın yerel ayarını değiştirir (çerez olarak). Tüm Paper hizmetleri için Paper yetkilendirme dilinizi değiştirmek için burayı tıklayın',
    },
    validation: {
        required: '{0} bulunmak zorunda',
        maxLength: 'Maksimum uzunluk {0}',
        minLength: 'Minimum uzunluk {0}',
        invalidFormat: '{0} geçersiz',
        invalidUrl: 'Geçersiz URL formatı',
    },
    prompts: {
        confirm: 'Anladım!',
        changeAvatar: {
            title: 'Avatarınızı değiştirin!',
            message: "Yeni Kuruluşunuza hoş geldiniz! İşe üzerine tıklayarak avatarını değiştirerek başlayın.",
        },
    },
    error: {
        userLocked: 'Hesabınız kilitli',
        401: 'Bunun için giriş yapmalısınız',
        403: 'Bunu yapmaya iznin yok',
        404: '404 sayfa bulunamadı',
        unknown: 'Bir hata gerçekleşti',
    },
};

export default msgs;

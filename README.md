# Kişisel Harcama Takip Uygulaması

Bu uygulama JavaFX, PostgreSQL, JDBC ve Maven kullanılarak geliştirilmiş bir kişisel harcama takip uygulamasıdır.

## Özellikler

- **Kullanıcı Sistemi:** Hesap oluşturma ve giriş yapma. Şifreler BCrypt ile hash'lenir.
- **Kategoriler:** Kullanıcılar kendi harcama kategorilerini ekleyebilir.
- **Harcamalar:** Harcama eklenebilir, düzenlenebilir, silinebilir ve listelenebilir.
- **Döviz Çevirici:** Harcamalar USD, EUR veya TRY cinsinden girilebilir. Yabancı para birimleri anlık kur (Frankfurter API) üzerinden TRY'ye çevrilerek kaydedilir.
- **Dışa Aktarma:** Harcamalar kolayca CSV formatında dışa aktarılabilir.
- **Arayüz:** FXML ve CSS kullanılarak modern ve temiz bir kullanıcı arayüzü tasarlanmıştır.

## Kurulum ve Çalıştırma

### 1. Veritabanı Yapılandırması
Uygulama PostgreSQL kullanmaktadır.
`src/main/resources/schema.sql` dosyasındaki tabloları oluşturmak için PostgreSQL veritabanınızda ilgili SQL sorgularını çalıştırın.

Root dizininde (bu dosyanın bulunduğu dizinde) `db.properties` isimli bir dosya oluşturun veya mevcut olanı güncelleyin:
```properties
db.url=jdbc:postgresql://localhost:5432/expense_tracker
db.user=postgres
db.password=sifreniz
```
*(Not: `db.properties` dosyası `.gitignore` a eklenmiştir ve repoya dahil edilmez.)*

### 2. Uygulamayı Derleme ve Çalıştırma
Uygulama Maven kullanmaktadır. Projeyi derlemek ve çalıştırmak için aşağıdaki komutları kullanabilirsiniz:

```bash
mvn clean install
mvn javafx:run
```

## Teknolojiler

- Java 17+
- JavaFX 17
- PostgreSQL (JDBC)
- org.mindrot:jbcrypt (Şifre Hashleme)
- com.google.code.gson (API JSON Parse işlemleri)
- Java HttpClient (REST API istekleri)

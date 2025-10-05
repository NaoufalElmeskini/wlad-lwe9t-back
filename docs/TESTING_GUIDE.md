# Guide de Test - API Stripe Backend

## Prérequis

### 1. Configuration des Variables d'Environnement

Créer un fichier `.env` à la racine du projet `wlad-lwe9t-back` :

```bash
# Stripe API Keys (mode test)
STRIPE_SECRET_KEY=sk_test_votre_cle_secrete_stripe
STRIPE_WEBHOOK_SECRET=whsec_test_votre_webhook_secret
```

**Obtenir les clés** :
1. Se connecter sur [dashboard.stripe.com](https://dashboard.stripe.com/test/apikeys)
2. Activer le mode **Test** (toggle en haut à droite)
3. Copier la **Clé secrète** (`sk_test_...`)
4. Pour le webhook secret, voir section "Configuration Webhook" ci-dessous

⚠️ **Important** : Ne jamais commiter le fichier `.env` dans Git !

### 2. Démarrer l'Application

```bash
# Construire l'application Java
mvn clean package -DskipTests

# Démarrer tous les services (PostgreSQL + Application)
docker-compose up -d

# Vérifier les logs
docker-compose logs -f
```


L'API démarre sur `http://localhost:8080/api`

Vérifier que l'application est bien démarrée :
```bash
curl http://localhost:8080/api/status
# Réponse attendue : {"status":"ok"}
```

## Tests avec Postman/Insomnia

### 1. Importer la Collection

**Postman** :
1. Ouvrir Postman
2. Cliquer sur **Import**
3. Sélectionner le fichier `docs/postman-collection.json`
4. La collection "Stripe Payment API - wladLwe9t" apparaît

**Insomnia** :
1. Ouvrir Insomnia
2. Cliquer sur **Create** > **Import From** > **File**
3. Sélectionner `docs/postman-collection.json`

### 2. Configuration de l'Authentification

Toutes les requêtes (sauf webhook et status) nécessitent une authentification **Basic Auth** :

- **Username** : `tintin`
- **Password** : `acrobate`

La collection est préconfigurée avec ces credentials.

### 3. Workflow de Test Complet

#### Étape 1 : Health Check

**Requête** : `GET /api/status`

Vérifier que l'API répond correctement.

**Réponse attendue** :
```json
{
  "status": "ok"
}
```

#### Étape 2 : Créer un Payment Intent

**Requête** : `POST /api/payments/create-intent`

**Body** :
```json
{
  "amount": 2500,
  "currency": "EUR",
  "customerInfo": {
    "email": "client@example.com",
    "firstName": "Jean",
    "lastName": "Dupont",
    "address": "123 Rue de Paris",
    "city": "Paris",
    "postalCode": "75001",
    "phone": "+33612345678"
  },
  "items": [
    {
      "id": "prod_001",
      "name": "T-shirt Premium",
      "quantity": 2,
      "price": 1250
    }
  ]
}
```

**Réponse attendue (200 OK)** :
```json
{
  "clientSecret": "pi_3ABC123_secret_XYZ789",
  "paymentIntentId": "pi_3ABC123",
  "amount": 2500,
  "currency": "EUR",
  "status": "REQUIRES_PAYMENT_METHOD"
}
```

**⚠️ Copier le `paymentIntentId` pour les étapes suivantes !**

#### Étape 3 : Récupérer le Statut du Payment Intent

**Requête** : `GET /api/payments/{paymentIntentId}`

Remplacer `{paymentIntentId}` par l'ID obtenu à l'étape 2.

**Exemple** :
```
GET http://localhost:8080/api/payments/pi_3ABC123
```

**Réponse attendue** :
```json
{
  "paymentIntentId": "pi_3ABC123",
  "amount": 2500,
  "currency": "EUR",
  "status": "REQUIRES_PAYMENT_METHOD"
}
```

#### Étape 4 : Vérifier dans le Dashboard Stripe

1. Aller sur [dashboard.stripe.com/test/payments](https://dashboard.stripe.com/test/payments)
2. Vérifier que le Payment Intent apparaît dans la liste
3. Cliquer dessus pour voir les détails (client, montant, items...)

## Tests avec Stripe CLI (Webhooks)

### 1. Installation de Stripe CLI

**Windows** :
```powershell
scoop install stripe
```

**macOS** :
```bash
brew install stripe/stripe-cli/stripe
```

**Linux** :
```bash
wget https://github.com/stripe/stripe-cli/releases/download/v1.19.0/stripe_1.19.0_linux_x86_64.tar.gz
tar -xvf stripe_1.19.0_linux_x86_64.tar.gz
sudo mv stripe /usr/local/bin/
```

### 2. Authentification

```bash
stripe login
```

Suivre les instructions pour se connecter à votre compte Stripe.

### 3. Écouter les Webhooks Localement

```bash
stripe listen --forward-to localhost:8080/api/payments/webhook
```

**Output** :
```
> Ready! Your webhook signing secret is whsec_abc123... (^C to quit)
```

**⚠️ Copier le webhook secret (`whsec_...`) et l'ajouter dans votre fichier `.env` !**

```bash
STRIPE_WEBHOOK_SECRET=whsec_abc123...
```

**Redémarrer l'application** pour prendre en compte la nouvelle variable.

### 4. Simuler un Événement Webhook

Dans un **nouveau terminal** :

```bash
# Simuler un paiement réussi
stripe trigger payment_intent.succeeded

# Simuler un paiement échoué
stripe trigger payment_intent.payment_failed

# Simuler une annulation
stripe trigger payment_intent.canceled
```

**Observer les logs** dans le terminal où tourne `stripe listen` :

```
2025-10-05 14:32:10   --> payment_intent.succeeded [evt_abc123]
2025-10-05 14:32:10  <--  [200] POST http://localhost:8080/api/payments/webhook
```

### 5. Tester un Webhook Réel

1. Créer un Payment Intent avec Postman (étape 2)
2. Aller sur [dashboard.stripe.com/test/payments](https://dashboard.stripe.com/test/payments)
3. Cliquer sur le Payment Intent créé
4. Simuler un événement (en haut à droite : **⋮ More** > **Send test webhook**)
5. Sélectionner `payment_intent.succeeded`
6. Observer les logs de l'application Spring Boot

**Logs attendus** :
```
Webhook received: payment_intent.succeeded for payment pi_3ABC123 (status: SUCCEEDED)
```

## Tests avec Cartes de Test Stripe

Pour tester un paiement complet (frontend + backend), utiliser ces cartes de test :

| Numéro CB        | Scénario              |
|------------------|-----------------------|
| 4242424242424242 | Paiement réussi       |
| 4000000000000002 | Carte refusée         |
| 4000000000009995 | Fonds insuffisants    |
| 4000002500003155 | 3D Secure requis      |

**Expiration** : N'importe quelle date future (ex: 12/30)
**CVC** : N'importe quel 3 chiffres (ex: 123)

## Erreurs Courantes et Solutions

### Erreur 401 Unauthorized

**Cause** : Authentification manquante ou incorrecte

**Solution** :
- Vérifier username : `tintin`
- Vérifier password : `acrobate`
- S'assurer que Basic Auth est activé dans Postman/Insomnia

### Erreur 400 Bad Request (Stripe)

**Cause** : Clé API Stripe invalide ou manquante

**Solution** :
1. Vérifier que `STRIPE_SECRET_KEY` est défini dans `.env`
2. Vérifier que la clé commence par `sk_test_` (mode test)
3. Redémarrer l'application après modification du `.env`

### Erreur Webhook Signature Invalid

**Cause** : `STRIPE_WEBHOOK_SECRET` incorrect ou manquant

**Solution** :
1. Lancer `stripe listen --forward-to localhost:8080/api/payments/webhook`
2. Copier le webhook secret affiché : `whsec_...`
3. Mettre à jour `STRIPE_WEBHOOK_SECRET` dans `.env`
4. Redémarrer l'application

### Erreur Amount Mismatch

**Cause** : Le montant total ne correspond pas à la somme des items

**Solution** :
```json
{
  "amount": 2500,  // DOIT être égal à : 1250 * 2 = 2500
  "items": [
    {
      "price": 1250,
      "quantity": 2
    }
  ]
}
```

## Exemples de Requêtes cURL

### Créer un Payment Intent

```bash
curl -X POST http://localhost:8080/api/payments/create-intent \
  -u tintin:acrobate \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000,
    "currency": "EUR",
    "customerInfo": {
      "email": "test@example.com",
      "firstName": "Test",
      "lastName": "User",
      "address": "123 Test St",
      "city": "Paris",
      "postalCode": "75001"
    },
    "items": [{
      "id": "test_001",
      "name": "Test Product",
      "quantity": 1,
      "price": 1000
    }]
  }'
```

### Récupérer un Payment Intent

```bash
curl -X GET http://localhost:8080/api/payments/pi_3ABC123 \
  -u tintin:acrobate
```

## Tests Automatisés

### Lancer les Tests

```bash
# Tests unitaires + intégration
mvn test

# Tests avec couverture
mvn test jacoco:report

# Rapport de couverture
open target/site/jacoco/index.html
```

### Tests Disponibles

- **PaymentServiceTest** : Tests unitaires du service métier
- **PaymentControllerIntegrationTest** : Tests d'intégration des endpoints API

## Ressources

- [Stripe API Documentation](https://stripe.com/docs/api)
- [Stripe CLI Documentation](https://stripe.com/docs/stripe-cli)
- [Cartes de Test Stripe](https://stripe.com/docs/testing)
- [Documentation Backend](./stripe-integration-backend.md)

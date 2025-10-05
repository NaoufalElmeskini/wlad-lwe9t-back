# Documentation Technique - Intégration Stripe Backend

## Prérequis

### 1. Compte Stripe et Clés API

#### Création du compte
1. Créer un compte sur [stripe.com](https://stripe.com)
2. Activer le mode test (toggle en haut à droite du dashboard)
3. Récupérer les clés API dans **Développeurs > Clés API**

#### Types de clés

**Mode Test (développement)**:
- **Clé publique** : `pk_test_...` (utilisée côté frontend)
- **Clé secrète** : `sk_test_...` (utilisée côté backend) ⚠️ **JAMAIS exposée publiquement**
- **Webhook secret** : `whsec_test_...` (pour valider les webhooks)

**Mode Production**:
- **Clé publique** : `pk_live_...`
- **Clé secrète** : `sk_live_...` ⚠️ **Sensible**
- **Webhook secret** : `whsec_...`

### 2. Variables d'Environnement

#### Fichier `.env` (local)
```bash
# Stripe Configuration
STRIPE_SECRET_KEY=sk_test_votre_cle_secrete_ici
STRIPE_WEBHOOK_SECRET=whsec_test_votre_webhook_secret_ici
```

#### Configuration application.yml
```yaml
stripe:
  api:
    secret-key: ${STRIPE_SECRET_KEY}
    webhook-secret: ${STRIPE_WEBHOOK_SECRET}
```

⚠️ **IMPORTANT** :
- Ne JAMAIS commiter les clés secrètes dans Git
- Ajouter `.env` au `.gitignore`
- Utiliser des variables d'environnement en production

### 3. Configuration Webhook Stripe

1. Aller dans **Développeurs > Webhooks** sur le dashboard Stripe
2. Créer un endpoint : `https://votre-domaine.com/api/payments/webhook`
3. Sélectionner les événements à écouter :
   - `payment_intent.succeeded`
   - `payment_intent.payment_failed`
   - `payment_intent.canceled`
4. Récupérer le **Webhook signing secret** (`whsec_...`)
5. En développement local, utiliser [Stripe CLI](https://stripe.com/docs/stripe-cli) :
```bash
stripe listen --forward-to localhost:8080/api/payments/webhook
```

## Architecture de l'Intégration

### Workflow Simplifié de Paiement

```
┌─────────────┐          ┌──────────────┐          ┌─────────────┐
│   Frontend  │          │   Backend    │          │   Stripe    │
└──────┬──────┘          └──────┬───────┘          └──────┬──────┘
       │                        │                         │
       │  1. Créer intention    │                         │
       │─────────────────────>  │                         │
       │                        │  2. PaymentIntent.create│
       │                        │─────────────────────────>│
       │                        │                         │
       │                        │  3. clientSecret        │
       │  clientSecret          │<─────────────────────────│
       │<───────────────────────│                         │
       │                        │                         │
       │  4. Confirmer paiement │                         │
       │  (Stripe.js + Card)    │                         │
       │──────────────────────────────────────────────────>│
       │                        │                         │
       │                        │  5. Webhook Event       │
       │                        │<─────────────────────────│
       │                        │  (payment_intent.*)     │
       │                        │                         │
       │  6. Résultat final     │                         │
       │<───────────────────────│                         │
       │                        │                         │
```

### Étapes Détaillées

#### Étape 1 : Création du Payment Intent (Backend)
Le backend reçoit les données du frontend et crée un **Payment Intent** chez Stripe.

**Requête Frontend → Backend** :
```http
POST /api/payments/create-intent
Content-Type: application/json
Authorization: Basic dGludGluOmFjcm9iYXRl

{
  "amount": 2500,
  "currency": "eur",
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
      "name": "T-shirt",
      "quantity": 2,
      "price": 1250
    }
  ]
}
```

**Réponse Backend** :
```json
{
  "clientSecret": "pi_3ABC123_secret_XYZ789",
  "paymentIntentId": "pi_3ABC123",
  "amount": 2500,
  "currency": "eur"
}
```

#### Étape 2 : Confirmation du Paiement (Frontend)
Le frontend utilise `clientSecret` avec Stripe.js pour finaliser le paiement (carte bancaire, PayPal...).

**Code Frontend (Angular)** :
```typescript
const result = await stripe.confirmPayment({
  elements,
  clientSecret: 'pi_3ABC123_secret_XYZ789',
  confirmParams: {
    return_url: 'https://your-site.com/payment-success'
  }
});
```

#### Étape 3 : Traitement des Webhooks (Backend)
Stripe envoie des événements au backend pour notifier le statut du paiement.

**Événements Stripe** :
- `payment_intent.succeeded` → Paiement réussi
- `payment_intent.payment_failed` → Échec du paiement
- `payment_intent.canceled` → Paiement annulé

**Exemple d'événement** :
```json
{
  "id": "evt_123",
  "type": "payment_intent.succeeded",
  "data": {
    "object": {
      "id": "pi_3ABC123",
      "amount": 2500,
      "status": "succeeded",
      "customer_email": "client@example.com"
    }
  }
}
```

## Endpoints API Backend

### 1. Créer Payment Intent

**Endpoint** : `POST /api/payments/create-intent`

**Headers** :
```http
Authorization: Basic dGludGluOmFjcm9iYXRl
Content-Type: application/json
```

**Body** :
```json
{
  "amount": 2500,
  "currency": "eur",
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
      "name": "T-shirt",
      "quantity": 2,
      "price": 1250
    }
  ]
}
```

**Réponse (200 OK)** :
```json
{
  "clientSecret": "pi_3ABC123_secret_XYZ789",
  "paymentIntentId": "pi_3ABC123",
  "amount": 2500,
  "currency": "eur"
}
```

**Erreurs** :
- `400 Bad Request` : Données invalides (montant négatif, devise non supportée...)
- `401 Unauthorized` : Authentification manquante
- `500 Internal Server Error` : Erreur Stripe API

### 2. Confirmer Paiement (Optionnel)

**Endpoint** : `POST /api/payments/confirm`

**Body** :
```json
{
  "paymentIntentId": "pi_3ABC123"
}
```

**Réponse (200 OK)** :
```json
{
  "success": true,
  "status": "succeeded",
  "paymentIntentId": "pi_3ABC123"
}
```

### 3. Webhook Stripe

**Endpoint** : `POST /api/payments/webhook`

**Headers** :
```http
Stripe-Signature: t=1234567890,v1=signature_hash
```

**Body** : Événement Stripe JSON brut

**Réponse** :
- `200 OK` : Événement traité avec succès
- `400 Bad Request` : Signature invalide
- `500 Internal Server Error` : Erreur traitement

## Sécurité

### 1. Validation des Webhooks
**OBLIGATOIRE** : Vérifier la signature Stripe pour éviter les faux webhooks.

```java
Event event = Webhook.constructEvent(
    payload,
    sigHeader,
    webhookSecret
);
```

### 2. Validation des Montants
**Toujours valider côté backend** :
- Montant > 0
- Devise supportée (EUR, USD...)
- Montant correspond aux items commandés

### 3. Clés API
- ✅ Clé secrète uniquement côté backend
- ✅ Variables d'environnement (jamais hardcodées)
- ✅ Rotation régulière des clés en production
- ❌ Jamais de clé secrète dans Git ou logs

### 4. HTTPS Obligatoire
- Production : HTTPS uniquement
- Développement : HTTP localhost autorisé

## Tests

### Cartes de Test Stripe

| Numéro              | Scénario                  |
|---------------------|---------------------------|
| 4242424242424242    | Paiement réussi           |
| 4000000000000002    | Carte refusée             |
| 4000000000009995    | Fonds insuffisants        |
| 4000002500003155    | Authentification 3D Secure|

**Expiration** : N'importe quelle date future (ex: 12/25)
**CVC** : N'importe quel 3 chiffres (ex: 123)

### Test avec Postman/Insomnia

#### 1. Créer Payment Intent
```bash
POST http://localhost:8080/api/payments/create-intent
Authorization: Basic dGludGluOmFjcm9iYXRl
Content-Type: application/json

{
  "amount": 1000,
  "currency": "eur",
  "customerInfo": {
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User",
    "address": "123 Test St",
    "city": "Paris",
    "postalCode": "75001"
  },
  "items": [
    {
      "id": "test_001",
      "name": "Test Product",
      "quantity": 1,
      "price": 1000
    }
  ]
}
```

#### 2. Simuler Webhook (Stripe CLI)
```bash
stripe trigger payment_intent.succeeded
```

## Concepts Stripe Essentiels

### 1. Payment Intent
Objet représentant l'**intention de paiement** avec :
- Montant et devise
- Statut (`requires_payment_method`, `processing`, `succeeded`, `canceled`)
- Méthodes de paiement autorisées (card, paypal...)

### 2. Client Secret
Token sécurisé permettant au frontend de finaliser le paiement **sans exposer la clé secrète backend**.

### 3. Webhook
Notification HTTP envoyée par Stripe au backend pour informer des changements d'état des paiements.

### 4. Idempotence
Stripe garantit l'idempotence via `Idempotency-Key` pour éviter les doublons de paiement.

## Ressources

- [Stripe API Docs](https://stripe.com/docs/api)
- [Payment Intents Guide](https://stripe.com/docs/payments/payment-intents)
- [Webhooks Guide](https://stripe.com/docs/webhooks)
- [Stripe Java SDK](https://github.com/stripe/stripe-java)
- [Stripe CLI](https://stripe.com/docs/stripe-cli)

## Workflow de Test Complet

### Étape 1 : Démarrer le backend
```bash
mvn spring-boot:run
```

### Étape 2 : Configurer Stripe CLI (webhooks locaux)
```bash
stripe login
stripe listen --forward-to localhost:8080/api/payments/webhook
# Copier le webhook secret affiché : whsec_...
```

### Étape 3 : Créer un Payment Intent (Postman)
```http
POST http://localhost:8080/api/payments/create-intent
```

### Étape 4 : Tester le paiement avec Stripe Dashboard
1. Aller sur [Dashboard Stripe > Paiements](https://dashboard.stripe.com/test/payments)
2. Trouver le Payment Intent créé
3. Simuler un événement (succeeded, failed...)

### Étape 5 : Vérifier les logs backend
Observer les webhooks reçus et traités dans les logs Spring Boot.

# PayLite Web

Modern React frontend for the PayLite wallet API.

## Stack

- **React 19** + **TypeScript** + **Vite**
- **Tailwind CSS v4**
- **TanStack Query** — server state & caching
- **React Hook Form** + **Zod** — forms & validation
- **React Router** — client routing
- **Axios** — API client with JWT interceptors

## Features

- Landing, signup, login (JWT stored in localStorage)
- Dashboard with balance, quick actions, recent transactions
- Add money (modal)
- P2P transfer with auto-generated idempotency keys
- Paginated transaction history
- Dark mode + responsive mobile layout

## Run locally

```bash
# Terminal 1 — backend (from repo root)
mvn -pl wallet-service -am spring-boot:run

# Terminal 2 — frontend
cd paylite-web
npm install
npm run dev
```

Open http://localhost:5173 — Vite proxies `/api` to http://localhost:8081.

## Production API

Create `.env`:

```env
VITE_API_URL=https://your-eb-url.elasticbeanstalk.com
```

Build:

```bash
npm run build
```

Deploy `dist/` to Vercel, Netlify, or S3 + CloudFront.

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

**Live app:** https://paylite-web.vercel.app

The frontend is deployed on Vercel. API calls use `/api/...` on the same origin — Vercel rewrites those requests to Elastic Beanstalk (see `vercel.json`). This avoids mixed-content issues because the EB URL is HTTP-only.

Leave `VITE_API_URL` empty in production (default). Vite dev proxy handles local; Vercel rewrites handle prod.

## Deploy to Vercel

From `paylite-web`:

```bash
npm run build          # optional local check
npx vercel deploy --prod
```

First-time setup links the folder to [Vercel](https://vercel.com). Connect your GitHub repo in the Vercel dashboard (root directory: `paylite-web`) for automatic deploys on push.

If you add HTTPS to the backend later, you can set `VITE_API_URL` in Vercel environment variables and remove the `/api` rewrite.

// Minimal client-side session storage for the logged-in user.
// The backend uses username-only auth (no tokens), so we just remember which
// user is "logged in" so the dashboard/trading pages know whose data to fetch.

export interface SessionUser {
  uid: number
  userName: string
  accountBal: number | string
}

const KEY = 'platypus.user'

export function saveUser(user: SessionUser): void {
  localStorage.setItem(KEY, JSON.stringify(user))
}

export function getUser(): SessionUser | null {
  const raw = localStorage.getItem(KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as SessionUser
  } catch {
    return null
  }
}

export function clearUser(): void {
  localStorage.removeItem(KEY)
}

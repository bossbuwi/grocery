import { useState } from "react";
import type { FormEvent } from "react";
import BrandMark from "../../shared/components/BrandMark";
import "./LoginPage.css";

type LoginPageProps = {
  onSignIn: (username: string, password: string) => Promise<void>;
};

export default function LoginPage({ onSignIn }: LoginPageProps) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      await onSignIn(username, password);
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : "We couldn't sign you in. Please try again.",
      );
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <main className="page-shell">
      <section className="login-card" aria-labelledby="page-title">
        <BrandMark />
        <p className="eyebrow">YOUR EVERYDAY, ORGANIZED</p>
        <h1 id="page-title">Welcome back</h1>
        <p className="intro">Sign in to continue to your grocery account.</p>

        <form className="login-form" onSubmit={handleSubmit}>
          <label htmlFor="username">Username</label>
          <input
            autoComplete="username"
            autoFocus
            id="username"
            name="username"
            onChange={(event) => setUsername(event.target.value)}
            required
            value={username}
          />

          <div className="password-label">
            <label htmlFor="password">Password</label>
          </div>
          <input
            autoComplete="current-password"
            id="password"
            name="password"
            onChange={(event) => setPassword(event.target.value)}
            required
            type="password"
            value={password}
          />

          {error && (
            <p className="error-message" role="alert">
              {error}
            </p>
          )}

          <button className="submit-button" disabled={isLoading} type="submit">
            {isLoading ? "Signing in..." : "Sign in"}
          </button>
        </form>

        <p className="footer-note">A little easier, one grocery run at a time.</p>
      </section>
    </main>
  );
}

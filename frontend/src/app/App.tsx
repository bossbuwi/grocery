import { useEffect, useState } from "react";
import { getCurrentUser, signIn } from "../features/auth/authApi";
import LoginPage from "../features/auth/LoginPage";
import DashboardPage from "../features/dashboard/DashboardPage";

const accessTokenKey = "grocery.accessToken";

type AuthenticatedSession = {
  username: string;
  accessToken: string;
};

export default function App() {
  const [session, setSession] = useState<AuthenticatedSession | null>(null);
  const [isCheckingSession, setIsCheckingSession] = useState(true);

  useEffect(() => {
    const accessToken = sessionStorage.getItem(accessTokenKey);
    if (!accessToken) {
      setIsCheckingSession(false);
      return;
    }

    getCurrentUser(accessToken)
      .then((user) => setSession({ username: user.username, accessToken }))
      .catch(() => sessionStorage.removeItem(accessTokenKey))
      .finally(() => setIsCheckingSession(false));
  }, []);

  async function handleSignIn(username: string, password: string) {
    const authenticatedSession = await signIn(username, password);
    sessionStorage.setItem(accessTokenKey, authenticatedSession.accessToken);
    setSession(authenticatedSession);
  }

  function handleSignOut() {
    sessionStorage.removeItem(accessTokenKey);
    setSession(null);
  }

  if (isCheckingSession) {
    return (
      <main className="page-shell">
        <p className="session-loading" role="status">
          Checking your session...
        </p>
      </main>
    );
  }

  if (session) {
    return (
      <DashboardPage
        accessToken={session.accessToken}
        onSignOut={handleSignOut}
        username={session.username}
      />
    );
  }

  return <LoginPage onSignIn={handleSignIn} />;
}

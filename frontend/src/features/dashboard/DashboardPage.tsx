import { useEffect, useState } from "react";
import type { ReactNode } from "react";
import BrandMark from "../../shared/components/BrandMark";
import StatCard from "./components/StatCard";
import { getDashboardSummary } from "./dashboardApi";
import type { DashboardSummary } from "./dashboardApi";
import "./DashboardPage.css";

type DashboardPageProps = {
  accessToken: string;
  onSignOut: () => void;
  username: string;
};

export default function DashboardPage({
  accessToken,
  onSignOut,
  username,
}: DashboardPageProps) {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [summaryError, setSummaryError] = useState<string | null>(null);
  const [isLoadingSummary, setIsLoadingSummary] = useState(true);
  const [retryCount, setRetryCount] = useState(0);

  useEffect(() => {
    let cancelled = false;

    async function loadSummary() {
      setIsLoadingSummary(true);
      setSummaryError(null);
      setSummary(null);

      try {
        const counts = await getDashboardSummary(accessToken);
        if (!cancelled) {
          setSummary(counts);
        }
      } catch {
        if (!cancelled) {
          setSummaryError("We couldn't load your totals. Please try again.");
        }
      } finally {
        if (!cancelled) {
          setIsLoadingSummary(false);
        }
      }
    }

    void loadSummary();
    return () => {
      cancelled = true;
    };
  }, [accessToken, retryCount]);

  const itemIcon: ReactNode = (
    <svg viewBox="0 0 24 24" fill="none">
      <path d="M5 8h14l-1.25 11H6.25L5 8Z" />
      <path d="m9 8 3-4 3 4M9 12v3m6-3v3" />
    </svg>
  );

  const storeIcon: ReactNode = (
    <svg viewBox="0 0 24 24" fill="none">
      <path d="M4 10v9h16v-9M3 10l1.5-5h15L21 10" />
      <path d="M3 10a2.5 2.5 0 0 0 5 0 2.5 2.5 0 0 0 5 0 2.5 2.5 0 0 0 5 0 2.5 2.5 0 0 0 3 0M9 19v-5h6v5" />
    </svg>
  );

  return (
    <main className="dashboard-shell">
      <header className="dashboard-header">
        <div className="dashboard-brand">
          <BrandMark compact />
          <span>Grocery</span>
        </div>
        <button className="dashboard-sign-out" onClick={onSignOut} type="button">
          Sign out
        </button>
      </header>

      <section className="dashboard-content" aria-labelledby="dashboard-title">
        <p className="eyebrow">YOUR GROCERY SPACE</p>
        <h1 id="dashboard-title">Hello, {username}.</h1>
        <p className="dashboard-intro">
          Welcome home. Your grocery dashboard is ready.
        </p>

        <div className="dashboard-stats" aria-busy={isLoadingSummary}>
          <StatCard
            caption="Grocery items in the catalog"
            category="COLLECTION"
            icon={itemIcon}
            label="Items saved"
            tone="items"
            value={isLoadingSummary ? "..." : (summary?.itemCount ?? "—")}
          />
          <StatCard
            caption="Stores available in the catalog"
            category="LOCATIONS"
            icon={storeIcon}
            label="Stores saved"
            tone="stores"
            value={isLoadingSummary ? "..." : (summary?.storeCount ?? "—")}
          />
        </div>

        {summaryError && (
          <div className="dashboard-summary-error" role="alert">
            <span>{summaryError}</span>
            <button
              className="retry-button"
              onClick={() => setRetryCount((count) => count + 1)}
              type="button"
            >
              Retry
            </button>
          </div>
        )}

        <div className="dashboard-welcome-card">
          <span className="dashboard-card-icon" aria-hidden="true">
            ✓
          </span>
          <div>
            <h2>You're all signed in</h2>
            <p>
              This is your home for grocery items, stores, and price tracking.
              We’ll build it out from here.
            </p>
          </div>
        </div>
      </section>
    </main>
  );
}

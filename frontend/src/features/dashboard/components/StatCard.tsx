import type { ReactNode } from "react";

type StatCardProps = {
  category: string;
  caption: string;
  icon: ReactNode;
  label: string;
  tone: "items" | "stores";
  value: number | string;
};

export default function StatCard({
  category,
  caption,
  icon,
  label,
  tone,
  value,
}: StatCardProps) {
  return (
    <article className="stat-card">
      <div className="stat-card-heading">
        <span className={`stat-icon stat-icon-${tone}`} aria-hidden="true">
          {icon}
        </span>
        <span className="stat-kicker">{category}</span>
      </div>
      <p className="stat-label">{label}</p>
      <strong className="stat-value">{value}</strong>
      <p className="stat-caption">{caption}</p>
    </article>
  );
}

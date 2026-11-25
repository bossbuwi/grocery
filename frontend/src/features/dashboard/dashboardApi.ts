import { requestJson } from "../../shared/api/httpClient";

export type DashboardSummary = {
  itemCount: number;
  storeCount: number;
};

export function getDashboardSummary(
  accessToken: string,
): Promise<DashboardSummary> {
  return requestJson<DashboardSummary>("/dashboard/summary", {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
}

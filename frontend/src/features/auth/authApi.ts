import { requestJson } from "../../shared/api/httpClient";

type LoginResponse = {
  accessToken: string;
};

export type CurrentUser = {
  username: string;
};

export type AuthenticatedSession = {
  username: string;
  accessToken: string;
};

export async function signIn(
  username: string,
  password: string,
): Promise<AuthenticatedSession> {
  const login = await requestJson<LoginResponse>("/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  const user = await getCurrentUser(login.accessToken);
  return { username: user.username, accessToken: login.accessToken };
}

export function getCurrentUser(accessToken: string): Promise<CurrentUser> {
  return requestJson<CurrentUser>("/auth/me", {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
}

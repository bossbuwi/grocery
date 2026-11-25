async function getApiError(response: Response): Promise<string> {
  try {
    const body: unknown = await response.json();
    if (
      typeof body === "object" &&
      body !== null &&
      "message" in body &&
      typeof body.message === "string"
    ) {
      return body.message;
    }
  } catch {
    // Use the status-based message below when the response has no JSON body.
  }

  if (response.status === 401) {
    return "That username and password combination was not recognized.";
  }
  return "We couldn't complete your request. Please try again.";
}

export async function requestJson<T>(
  path: string,
  init?: RequestInit,
): Promise<T> {
  const response = await fetch(path, init);
  if (!response.ok) {
    throw new Error(await getApiError(response));
  }
  return response.json() as Promise<T>;
}

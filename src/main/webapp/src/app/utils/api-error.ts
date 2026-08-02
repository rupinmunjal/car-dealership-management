export function getApiErrorMessage(error: unknown, fallback: string): string {
  const payload = (error as { error?: { message?: unknown; error?: unknown } })?.error;
  const message = payload?.message;

  if (typeof message === 'string' && message.trim()) {
    return message;
  }

  if (message && typeof message === 'object') {
    const details = Object.values(message as Record<string, unknown>)
      .filter((value): value is string => typeof value === 'string' && value.trim().length > 0);
    if (details.length > 0) {
      return details.join(' ');
    }
  }

  return typeof payload?.error === 'string' && payload.error.trim()
    ? payload.error
    : fallback;
}

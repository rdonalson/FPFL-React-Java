export function normalizeAuthResponse(res: any) {
  return {
    accessToken: res.accessToken,
    refreshToken: res.refreshToken,
    id: res.id,
    userId: res.userId,
    email: res.email,
    first: res.first,
    last: res.last,
    roles: res.roles,
    raw: res,
  };
}

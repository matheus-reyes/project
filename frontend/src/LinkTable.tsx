import type { ShortLinkListItem } from './api'

type Props = {
  links: ShortLinkListItem[]
  // Time the list was fetched; the "expired" badge compares against it so render stays pure
  fetchedAt: number
}

function isExpired(link: ShortLinkListItem, at: number) {
  return link.expiresAt !== null && Date.parse(link.expiresAt) <= at
}

function LinkTable({ links, fetchedAt }: Props) {
  if (links.length === 0) {
    return <p className="empty">No links yet. Shorten one above.</p>
  }

  return (
    <table className="link-table">
      <thead>
        <tr>
          <th>Code</th>
          <th>Original URL</th>
          <th className="numeric">Clicks</th>
          <th>Short URL</th>
        </tr>
      </thead>
      <tbody>
        {links.map((link) => (
          <tr key={link.shortCode}>
            <td>
              <code>{link.shortCode}</code>
              {isExpired(link, fetchedAt) && <span className="badge">expired</span>}
            </td>
            <td className="original-url" title={link.originalUrl}>
              {link.originalUrl}
            </td>
            <td className="numeric">{link.clickCount}</td>
            <td>
              <a href={link.shortUrl} target="_blank" rel="noreferrer">
                {link.shortUrl}
              </a>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}

export default LinkTable

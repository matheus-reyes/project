# URL Shortener

Turns long URLs into short ones, redirects visitors, and records each visit so the link's creator can see how it performs.

## Links

**Short Link**:
A mapping from a Short Code to an Original URL, with a creation time and an optional expiration. Each creation produces a new Short Link, even for an Original URL that was already shortened.
_Avoid_: URL, alias, short URL (when meaning the whole record)

**Short Code**:
The 7-character alphanumeric identifier of a Short Link. It is random, so codes cannot be enumerated.
_Avoid_: slug, key, hash, id

**Short URL**:
The full address a visitor opens, made of the service's base address and the Short Code.
_Avoid_: link (alone), tiny URL

**Original URL**:
The http or https address a Short Link redirects to.
_Avoid_: target, long URL, destination

**Expired Link**:
A Short Link whose expiration time has passed. It no longer redirects, but it still appears in listings and keeps its Analytics.
_Avoid_: deleted link, inactive link

## Tracking

**Click**:
One successful redirect through a Short Link, recorded with its time, Referrer, user agent, and IP address. A visit to an Expired Link is not a Click.
_Avoid_: hit, visit, access, view

**Referrer**:
The host of the page a Click came from, or `direct` when the visitor arrived without one.
_Avoid_: source, origin

**Analytics**:
The summary of a Short Link's Clicks: total Clicks, Clicks per UTC day over the last 30 days, and the top Referrers. Anyone who knows the Short Code can view it.
_Avoid_: stats (outside the API path), report, metrics

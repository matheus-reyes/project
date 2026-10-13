import type { Stats } from './api'

type Props = {
  days: Stats['clicksByDay']
}

function formatDay(date: string) {
  // Dates are UTC calendar days; format them in UTC so they do not shift a day
  return new Date(`${date}T00:00:00Z`).toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    timeZone: 'UTC',
  })
}

function ClicksChart({ days }: Props) {
  const max = Math.max(1, ...days.map((day) => day.count))

  return (
    <figure className="chart">
      <div className="chart-plot">
        <span className="chart-max">{max}</span>
        <div className="chart-bars" role="list" aria-label="Clicks per day">
          {days.map((day) => {
            const label = `${formatDay(day.date)}: ${day.count} ${day.count === 1 ? 'click' : 'clicks'}`
            return (
              <div key={day.date} className="chart-slot" role="listitem" aria-label={label}>
                <span className="chart-tooltip">{label}</span>
                {day.count > 0 && (
                  <div className="chart-bar" style={{ height: `${(day.count / max) * 100}%` }} />
                )}
              </div>
            )
          })}
        </div>
      </div>
      <figcaption className="chart-axis">
        <span>{formatDay(days[0].date)}</span>
        <span>Today (UTC)</span>
      </figcaption>
    </figure>
  )
}

export default ClicksChart

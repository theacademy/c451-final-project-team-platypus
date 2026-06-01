import React, { useState, useEffect } from 'react'
import DashboardNav from '../../components/DashboardNav'
import Footer from '../../components/Footer'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'
import { getUser } from '../../lib/session'
import {
  ResponsiveContainer,
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  XAxis,
  YAxis,
} from 'recharts'

interface MarketStock {
  sid: number
  stockCode: string
  stockName: string
  price: number
  change1d: number | null
  change7d: number | null
  change30d: number | null
  change1y: number | null
}

interface Portfolio {
  date: string
  cash: number
  stockValue: number
  total: number
  simulationOver: boolean
}

const BuyStocks = () => {
  const navigate = useNavigate()
  const user = getUser()

  const [stocks, setStocks] = useState<MarketStock[]>([])
  const [selected, setSelected] = useState<MarketStock | null>(null)
  const [stockAmt, setStockAmt] = useState(1)
  const [portfolio, setPortfolio] = useState<Portfolio | null>(null)
  const [message, setMessage] = useState('')

  // forex/crypto remain mock data for now (backend tracks stocks only)
  const [forex] = useState([
    { abbr: 'USD', price: '25', change: '1.6' },
    { abbr: 'GBPD', price: '30', change: '3.4' },
    { abbr: 'JPY', price: '5', change: '2.8' },
  ])
  const [crypto] = useState([
    { abbr: 'BTCN', price: '75303', change: '24' },
    { abbr: 'ETRM', price: '20405', change: '5.4' },
  ])

  const [history, setHistory] = useState<{ date: string; price: number }[]>([])

  const refresh = async () => {
    if (!user) return
    try {
      const [mkt, state] = await Promise.all([
        axios.get('/api/stocks'),
        axios.get(`/api/sim/state/${user.uid}`),
      ])
      setStocks(mkt.data)
      setPortfolio(state.data)
      setSelected((prev) =>
        prev
          ? (mkt.data.find((s: MarketStock) => s.sid === prev.sid) ??
            mkt.data[0])
          : mkt.data[0],
      )
    } catch (err) {
      console.error(err)
    }
  }

  useEffect(() => {
    if (!user) {
      navigate('/Login')
      return
    }
    refresh()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Load price history whenever the selected stock (or the sim date) changes.
  useEffect(() => {
    if (!selected) return
    axios
      .get(`/api/stocks/${selected.sid}/history`)
      .then((res) =>
        setHistory(
          res.data.map((p: { date: string; price: string | number }) => ({
            date: p.date,
            price: Number(p.price),
          })),
        ),
      )
      .catch((err) => console.error(err))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selected?.sid, portfolio?.date])

  const trade = async (action: 'buy' | 'sell') => {
    if (!user || !selected) return
    setMessage('')
    try {
      const res = await axios.post(`/api/stocks/${action}`, {
        uid: user.uid,
        sid: selected.sid,
        quantity: stockAmt,
      })
      setMessage(res.data.message)
      await refresh()
    } catch (err) {
      setMessage('Trade failed.')
      console.error(err)
    }
  }

  const fmtPct = (p: number | null) =>
    p === null ? 'N/A' : `${p >= 0 ? '+' : ''}${p}%`

  return (
    <div className="flex flex-col min-h-svh">
      <DashboardNav />
      <div className="container mx-auto flex-grow">
        <div className="border p-2 flex flex-grow gap-4">
          {/* watchlist */}
          <div className="border p-2">
            <div className="grid grid-cols-5 gap-6 p-2 font-bold border-b">
              <div>Symbol</div>
              <div>Price</div>
              <div>1D</div>
              <div>7D</div>
              <div>30D</div>
            </div>
            <div className="font-bold mt-2">Stocks</div>
            {stocks.map((stock) => (
              <div
                key={stock.sid}
                onClick={() => setSelected(stock)}
                className={`grid grid-cols-5 gap-6 p-2 cursor-pointer rounded ${
                  selected?.sid === stock.sid
                    ? 'bg-indigo-100'
                    : 'hover:bg-gray-100'
                }`}
              >
                <div>{stock.stockCode}</div>
                <div>${stock.price}</div>
                <div>{fmtPct(stock.change1d)}</div>
                <div>{fmtPct(stock.change7d)}</div>
                <div>{fmtPct(stock.change30d)}</div>
              </div>
            ))}

            <div className="font-bold mt-3">Forex (demo)</div>
            {forex.map((f) => (
              <div
                key={f.abbr}
                className="grid grid-cols-5 gap-6 p-2 opacity-70"
              >
                <div>{f.abbr}</div>
                <div>{f.price}</div>
                <div>{f.change}</div>
              </div>
            ))}
            <div className="font-bold mt-3">Crypto (demo)</div>
            {crypto.map((c) => (
              <div
                key={c.abbr}
                className="grid grid-cols-5 gap-6 p-2 opacity-70"
              >
                <div>{c.abbr}</div>
                <div>{c.price}</div>
                <div>{c.change}</div>
              </div>
            ))}
          </div>

          {/* chart + purchase ui */}
          <div className="flex flex-col flex-grow min-w-0">
            <div style={{ width: '100%', maxWidth: 600, aspectRatio: 1.618 }}>
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={history}>
                  <CartesianGrid />
                  <Line dataKey="price" dot={false} />
                  <XAxis dataKey="date" hide />
                  <YAxis domain={['auto', 'auto']} />
                  <Legend />
                </LineChart>
              </ResponsiveContainer>
            </div>

            <div className="border p-2 rounded-lg mt-4">
              <div className="p-2 text-[24px]">
                Purchase {selected ? selected.stockCode : 'Stock'}
              </div>
              <div>Current Balance</div>
              <div className="text-[20px]">
                ${portfolio ? portfolio.cash.toLocaleString() : '—'}
              </div>
              {selected && (
                <div className="mt-1">
                  Price: ${selected.price} · Est. cost: $
                  {(selected.price * stockAmt).toLocaleString()}
                </div>
              )}
              <div className="flex gap-3 items-center mt-3">
                <span>Quantity: {stockAmt}</span>
                <button
                  onClick={() => setStockAmt((q) => Math.max(1, q - 1))}
                  className="text-[18px] border px-3 rounded"
                >
                  −
                </button>
                <button
                  onClick={() => setStockAmt((q) => q + 1)}
                  className="text-[18px] border px-3 rounded"
                >
                  +
                </button>
              </div>
              <div className="flex gap-3 mt-4">
                <button
                  onClick={() => trade('buy')}
                  className="bg-green-600 text-white rounded px-4 py-2 hover:bg-green-700 disabled:opacity-50"
                  disabled={!selected}
                >
                  Buy Stock
                </button>
                <button
                  onClick={() => trade('sell')}
                  className="bg-red-600 text-white rounded px-4 py-2 hover:bg-red-700 disabled:opacity-50"
                  disabled={!selected}
                >
                  Sell Stock
                </button>
              </div>
              {message && <div className="mt-3 text-sm">{message}</div>}
            </div>
          </div>
        </div>
      </div>
      <Footer />
    </div>
  )
}

export default BuyStocks

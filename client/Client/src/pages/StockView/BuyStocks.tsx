import React, { useState, useEffect } from "react";
import DashboardNav from "../../components/DashboardNav";
import Footer from "../../components/Footer";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { getUser } from "../../lib/session";
import {
  ResponsiveContainer,
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  XAxis,
  YAxis,
} from "recharts";

interface MarketStock {
  sid: number;
  stockCode: string;
  stockName: string;
  price: number;
  change1d: number | null;
  change7d: number | null;
  change30d: number | null;
  change1y: number | null;
}

interface Portfolio {
  date: string;
  cash: number;
  stockValue: number;
  total: number;
  simulationOver: boolean;
}

interface OwnedStock {
  sid: number;
  shares: number;
}

const BuyStocks = () => {
  const navigate = useNavigate();
  const user = getUser();

  const [stocks, setStocks] = useState<MarketStock[]>([]);
  const [selected, setSelected] = useState<MarketStock | null>(null);
  const [stockAmt, setStockAmt] = useState(1);
  const [portfolio, setPortfolio] = useState<Portfolio | null>(null);
  const [message, setMessage] = useState("");
  const [ownedMap, setOwnedMap] = useState<Record<number, number>>({});

  const [history, setHistory] = useState<{ date: string; price: number }[]>([]);

  const refresh = async () => {
    if (!user) return;
    try {
      const [mkt, state, ownedRes] = await Promise.all([
        axios.get("/api/stocks"),
        axios.get(`/api/sim/state/${user.uid}`),
        axios.get(`/api/stocks/owned/${user.uid}`),
      ]);
      setStocks(mkt.data);
      setPortfolio(state.data);
      const map: Record<number, number> = {};
      for (const o of ownedRes.data as OwnedStock[]) {
        map[o.sid] = o.shares;
      }
      setOwnedMap(map);
      setSelected((prev) =>
        prev
          ? (mkt.data.find((s: MarketStock) => s.sid === prev.sid) ??
            mkt.data[0])
          : mkt.data[0],
      );
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    if (!user) {
      navigate("/Login");
      return;
    }
    refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Load price history whenever the selected stock (or the sim date) changes.
  useEffect(() => {
    if (!selected) return;
    axios
      .get(`/api/stocks/${selected.sid}/history?days=30`)
      .then((res) =>
        setHistory(
          Object.entries(res.data).map(([date, price]) => ({
            date,
            price: Number(price),
          })),
        ),
      )
      .catch((err) => console.error(err));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selected?.sid, portfolio?.date]);

  const trade = async (action: "buy" | "sell") => {
    if (!user || !selected) return;
    setMessage("");
    try {
      const res = await axios.post(`/api/stocks/${action}`, {
        uid: user.uid,
        sid: selected.sid,
        quantity: stockAmt,
      });
      setMessage(res.data.message);
      await refresh();
    } catch (err) {
      setMessage("Trade failed.");
      console.error(err);
    }
  };

  const restart = async () => {
    if (!user) return;
    try {
      const res = await axios.post(`/api/sim/restart?uid=${user.uid}`);
      setPortfolio(res.data);
      setOwnedMap({});
      await refresh();
    } catch (err) {
      console.error(err);
    }
  };

  const fmtPct = (p: number | null) =>
    p === null ? "N/A" : `${p >= 0 ? "+" : ""}${p}%`;

  return (
    <div className="flex flex-col min-h-svh bg-gray-950">
      <DashboardNav />
      <div className="container mx-auto grid grid-cols-2 gap-10 my-10">
        {/* watchlist */}
        <div className="min-w-0 border rounded-[20px] p-2 overflow-auto bg-gray-700 text-white">
          <div className="grid grid-cols-7 gap-3 p-2 font-bold border-b">
            <div>Symbol</div>
            <div>Name</div>
            <div className="text-right">Price</div>
            <div className="text-right">Owned</div>
            <div className="text-right">1D</div>
            <div className="text-right">7D</div>
            <div className="text-right">30D</div>
          </div>
          <div className="font-bold mt-2">Stocks</div>
          {stocks.map((stock) => (
            <div
              key={stock.sid}
              onClick={() => setSelected(stock)}
              className={`grid grid-cols-7 gap-3 p-2 cursor-pointer rounded ${
                selected?.sid === stock.sid
                  ? "bg-indigo-500"
                  : "hover:bg-gray-500"
              }`}
            >
              <div>{stock.stockCode}</div>
              <div className="truncate">{stock.stockName}</div>
              <div className="text-right">${stock.price}</div>
              <div className="text-right">{ownedMap[stock.sid] ?? 0}</div>
              <div className="text-right">{fmtPct(stock.change1d)}</div>
              <div className="text-right">{fmtPct(stock.change7d)}</div>
              <div className="text-right">{fmtPct(stock.change30d)}</div>
            </div>
          ))}
        </div>

        {/* chart and purchase ui */}
        <div className="min-w-0 flex flex-col gap-10">
          <div className="border rounded-[20px] bg-gray-700">
            <div style={{ width: "100%", height: 450 }}>
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={history}>
                  <CartesianGrid />
                  <Line dataKey="price" dot={false} />
                  <XAxis
                    dataKey="date"
                    tick={{ fontSize: 12 }}
                    interval={Math.max(0, Math.floor(history.length / 5) - 1)}
                    angle={-35}
                    textAnchor="end"
                    height={60}
                  />
                  <YAxis domain={["auto", "auto"]} />
                  <Legend />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className=" p-3 rounded-[20px] bg-gray-700 text-white">
            <div className="p-2 text-[24px]">
              Purchase {selected ? selected.stockCode : "Stock"}
            </div>
            <div>Current Date</div>
            <div className="text-[20px]">{portfolio?.date ?? "—"}</div>
            <div className="mt-2">Current Balance</div>
            <div className="text-[20px]">
              ${portfolio ? portfolio.cash.toLocaleString() : "—"}
            </div>
            {selected && (
              <div className="mt-1">
                Shares owned: {ownedMap[selected.sid] ?? 0}
              </div>
            )}
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
                onClick={() => trade("buy")}
                className="bg-gray-950 text-white rounded-[15px] px-4 py-2 hover:bg-gray-800 disabled:opacity-50"
                disabled={!selected}
              >
                Buy Stock
              </button>
              <button
                onClick={() => trade("sell")}
                className="bg-gray-950 text-white rounded-[15px] px-4 py-2 hover:bg-gray-800 disabled:opacity-50"
                disabled={!selected}
              >
                Sell Stock
              </button>
            </div>
            {message && <div className="mt-3 text-sm">{message}</div>}
            {portfolio?.simulationOver && (
              <div className="mt-4 flex items-center gap-4">
                <div className="text-amber-600 font-bold">
                  Simulation complete.
                </div>
                <button
                  onClick={restart}
                  className="bg-indigo-500 hover:bg-indigo-600 text-white rounded-lg px-4 py-2 transition-colors"
                >
                  Restart Simulation
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
      <Footer />
    </div>
  );
};

export default BuyStocks;

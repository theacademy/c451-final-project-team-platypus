import React from "react";
import DashboardNav from "../../components/DashboardNav";
import Footer from "../../components/Footer";
import { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { getUser } from "../../lib/session";

interface Portfolio {
  date: string;
  cash: number;
  stockValue: number;
  total: number;
  simulationOver: boolean;
}

interface OwnedStock {
  sid: number;
  stockCode: string;
  stockName: string;
  price: number;
  shares: number;
  value: number;
}

const Dashboard = () => {
  const navigate = useNavigate();
  const user = getUser();

  const [portfolio, setPortfolio] = useState<Portfolio | null>(null);
  const [owned, setOwned] = useState<OwnedStock[]>([]);
  const [loading, setLoading] = useState(true);

  const loadData = async (uid: number) => {
    setLoading(true);
    try {
      const [stateRes, ownedRes] = await Promise.all([
        axios.get(`/api/sim/state/${uid}`),
        axios.get(`/api/stocks/owned/${uid}`),
      ]);
      setPortfolio(stateRes.data);
      setOwned(ownedRes.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!user) {
      navigate("/Login");
      return;
    }
    loadData(user.uid);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const advance = async (days: number) => {
    if (!user) return;
    try {
      const res = await axios.post(
        `/api/sim/advance?uid=${user.uid}&days=${days}`,
      );
      setPortfolio(res.data);
      // refresh holdings (prices/values change with the date)
      const ownedRes = await axios.get(`/api/stocks/owned/${user.uid}`);
      setOwned(ownedRes.data);
    } catch (err) {
      console.error(err);
    }
  };

  const restart = async () => {
    if (!user) return;
    try {
      const res = await axios.post(`/api/sim/restart?uid=${user.uid}`);
      setPortfolio(res.data);
      setOwned([]);
    } catch (err) {
      console.error(err);
    }
  };

  const fmt = (n: number | undefined) =>
    n === undefined
      ? "—"
      : n.toLocaleString(undefined, {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2,
        });

  return (
    <div className="flex flex-col min-h-svh bg-gray-950">
      <DashboardNav />
      <div className="container mx-auto flex flex-grow my-10 gap-10">
        {/* progress/time skip */}
        <div className="flex-1 flex flex-col gap-10">
          {/* account summary */}
          <div className="flex-1 border rounded-lg border-black text-white bg-gray-700 p-5">
            <div className="flex text-center text-[32px] p-1 border-b items-stretch">
              Account Summary
            </div>
            <div className="grid grid-cols-2 p-1 gap-5">
              <div>
                <div>Cash Balance</div>
                <div className="text-[28px]">${fmt(portfolio?.cash)}</div>
              </div>
              <div>
                <div>Stock Value</div>
                <div className="text-[28px]">${fmt(portfolio?.stockValue)}</div>
              </div>
              <div>
                <div>Total Portfolio</div>
                <div className="text-[28px]">${fmt(portfolio?.total)}</div>
              </div>
              <div>
                <div>Current Date</div>
                <div className="text-[28px]">{portfolio?.date ?? "—"}</div>
              </div>
            </div>
            {portfolio?.simulationOver && (
              <div className="mt-3 flex items-center gap-4">
                <div className="text-amber-300">Simulation complete.</div>
                <button
                  onClick={restart}
                  className="bg-indigo-500 hover:bg-indigo-600 text-white rounded-lg px-4 py-2 transition-colors"
                >
                  Restart Simulation
                </button>
              </div>
            )}
          </div>
          {/* time skip */}
          <div className="flex-1 flex flex-col border border-black rounded-[20px] bg-gray-700 text-white p-5">
            <div className="text-[24px]">Time Skipper</div>
            <div className="mt-2">
              Pick a span, then advance the simulation.
            </div>
            <div className="flex p-2 gap-2 bg-gray-950 border rounded-[20px] justify-evenly mt-5">
              <button
                onClick={() => advance(1)}
                className="border rounded-[15px] p-2 flex-grow bg-gray-700 hover:bg-gray-600 focus:bg-indigo-500"
              >
                1D
              </button>
              <button
                onClick={() => advance(7)}
                className="border rounded-[15px] p-2 flex-grow bg-gray-700 hover:bg-gray-600 focus:bg-indigo-500"
              >
                1W
              </button>
              <button
                onClick={() => advance(30)}
                className="border rounded-[15px] p-2 flex-grow bg-gray-700 hover:bg-gray-600 focus:bg-indigo-500"
              >
                1M
              </button>
              <button
                onClick={() => advance(365)}
                className="border rounded-[15px] p-2 flex-grow bg-gray-700 hover:bg-gray-600 focus:bg-indigo-500"
              >
                1Y
              </button>
            </div>
          </div>
        </div>
        {/* holdings */}
        <div className="flex-1 flex flex-col border border-black rounded-lg bg-gray-700 text-white p-5">
          <div className="text-[32px] border-b">Currently Owned Stocks</div>
          {loading ? (
            <div className="p-4">Loading…</div>
          ) : owned.length === 0 ? (
            <div className="p-4 opacity-70">You don't own any stocks yet.</div>
          ) : (
            <div>
              <div className="grid grid-cols-4 gap-6 p-2 pl-6 font-bold border-b">
                <div>Symbol</div>
                <div>Price</div>
                <div>Shares</div>
                <div>Value</div>
              </div>
              {owned.map((s) => (
                <div key={s.sid} className="grid grid-cols-4 gap-6 p-2 pl-6">
                  <div>{s.stockCode}</div>
                  <div>${fmt(s.price)}</div>
                  <div>{s.shares}</div>
                  <div>${fmt(s.value)}</div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
      <Footer />
    </div>
  );
};

export default Dashboard;

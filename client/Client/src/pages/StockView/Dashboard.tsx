import React from 'react'
import DashboardNav from '../../components/DashboardNav'
import Footer from '../../components/Footer'
import { useState } from 'react'

const Dashboard = () => {
  const [user, setUser] = useState([
    {
      name: 'Dennis',
      totalAccValue: '100,000',
      totalProfit: '5000',
      totalPercentGrowth: '3.6%',
      totalTimePassed: '20',
    },
  ])

  const [stocks, setStocks] = useState([
    {
      category: 'stock',
      name: 'Apple',
      abbr: 'AAPL',
      price: '312.06',
      change: '2.5',
    },
    {
      category: 'stock',
      name: 'Netflix',
      abbr: 'NFLX',
      price: '200',
      change: '1.5',
    },
    {
      category: 'stock',
      name: 'Tesla',
      abbr: 'tsla',
      price: '300',
      change: '1.2',
    },
  ])
  const [forex, setForex] = useState([
    {
      category: 'forex',
      name: 'United States Dollar',
      abbr: 'USD',
      price: '25',
      change: '1.6',
    },
    {
      category: 'forex',
      name: 'British Pound',
      abbr: 'GBPD',
      price: '30',
      change: '3.4',
    },
    {
      category: 'forex',
      name: 'Japanese Yen',
      abbr: 'JPY',
      price: '5',
      change: '2.8',
    },
  ])
  const [crypto, setCrypto] = useState([
    {
      category: 'crypto',
      name: 'BitCoin',
      abbr: 'BTCN',
      price: '75303',
      change: '24',
    },
    {
      category: 'crypto',
      name: 'etherium',
      abbr: 'ETRM',
      price: '20405',
      change: '5.4',
    },
  ])

  return (
    <div className="flex flex-col min-h-svh bg-gray-950">
      <DashboardNav />
      <div className="container mx-auto flex flex-grow my-10 gap-10">
        {/* progress/time skip */}
        <div className="flex-1 flex flex-col gap-10">
          {/* progress */}
          <div className="flex-1 border rounded-lg border-black text-white bg-gray-700 p-5">
            <div className="flex text-center text-[32px] p-1 border-b items-stretch">
              Account Summary
            </div>
            <div className="grid grid-cols-2 p-1 gap-5">
              <div>
                <div>Total Account Value</div>
                <div className="text-[28px]"> {user[0].totalAccValue} </div>
              </div>
              <div>
                <div>Total Profit</div>
                <div className="text-[28px]"> {user[0].totalProfit} </div>
              </div>
              <div>
                <div>Average Growth %</div>
                <div className="text-[28px]"> {user[0].totalPercentGrowth}</div>
              </div>
              <div>
                <div>Total Days Elapsed</div>
                <div className="text-[28px]"> {user[0].totalTimePassed} </div>
              </div>
            </div>
            <div className="flex"></div>
          </div>
          {/* time skip */}
          <div className="flex-1 flex flex-col border border-black rounded-[20px] bg-gray-700 text-white p-5">
            <div className="text-[24px]">Time Skipper</div>
            <div className="mt-2">Pick desired time, then hit simulate!</div>
            <div className="flex p-2 gap-2 bg-gray-950 border rounded-[20px] justify-evenly mt-5">
              <button className="border rounded-[15px] p-2 flex-grow bg-gray-700 hover:bg-gray-600 focus:bg-indigo-500">
                1D
              </button>
              <button className="border rounded-[15px] p-2 flex-grow bg-gray-700 hover:bg-gray-600 focus:bg-indigo-500">
                1W
              </button>
              <button className="border rounded-[15px] p-2 flex-grow bg-gray-700 hover:bg-gray-600 focus:bg-indigo-500">
                1M
              </button>
            </div>
            <button className="border rounded-[10px] py-5 w-50 self-center bg-gray-900 mt-5 hover:bg-gray-600 active:bg-gray-700">
              skip
            </button>
          </div>
        </div>
        {/* graph */}
        <div className="flex-1 flex flex-col border border-black rounded-lg bg-gray-700 text-white p-5">
          <div className="text-[32px] border-b">Currently Owned Stocks</div>
          <div>
            {stocks.map((stock) => {
              return (
                <div className="grid grid-cols-3 gap-10 p-2 pl-20">
                  <div> {stock.abbr} </div>
                  <div> {stock.price} </div>
                  <div> {stock.change} </div>
                </div>
              )
            })}
          </div>
          <div>
            <div>Forex</div>
            {forex.map((forex) => {
              return (
                <div className="grid grid-cols-3 gap-10 p-2 pl-20">
                  <div> {forex.abbr} </div>
                  <div> {forex.price} </div>
                  <div> {forex.change} </div>
                </div>
              )
            })}
          </div>
          <div>
            <div>Crypto</div>
            {crypto.map((crypto) => {
              return (
                <div className="grid grid-cols-3 gap-10 p-2 pl-20">
                  <div> {crypto.abbr} </div>
                  <div> {crypto.price} </div>
                  <div> {crypto.change} </div>
                </div>
              )
            })}
          </div>
        </div>
      </div>
      <Footer />
    </div>
  )
}

export default Dashboard

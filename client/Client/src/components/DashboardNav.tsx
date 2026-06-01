import { Link } from 'react-router-dom'

const DashboardNav = () => {
  return (
    <div>
      <div className="flex justify-between p-5 bg-gray-900 text-white border-b border-gray-800">
        {/* left side */}
        <div className="flex gap-5">
          <div className="p-2">StockSim</div>
          <div className="flex">
            <Link
              to="/Dashboard"
              className="p-2 text-gray-300 hover:text-white"
            >
              To Dashboard Page
            </Link>
            <Link
              to="/BuyStocks"
              className="p-2 text-gray-300 hover:text-white"
            >
              To Buy Stocks Page
            </Link>
          </div>
        </div>
        {/* right side */}
        <div className="flex gap-5">
          <div className="p-2">Welcome Dennis</div>
          <Link
            to="/"
            className="border border-gray-700 rounded p-2 hover:bg-gray-800"
          >
            Logout
          </Link>
        </div>
      </div>
    </div>
  )
}

export default DashboardNav

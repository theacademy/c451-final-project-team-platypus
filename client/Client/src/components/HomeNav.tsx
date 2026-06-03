import { Link } from 'react-router-dom'

const HomeNav = () => {
  return (
    <div>
      <div className="flex justify-between p-5 bg-gray-900 text-white border-b border-gray-800">
        {/* left side */}
        <div className="flex gap-5">
          <div className="p-2">StockSim</div>
          {/* <div className="flex">
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
          </div> */}
        </div>
        {/* right side */}
        <div className="flex gap-5">
          <Link
            to="/Register"
            className="rounded-[16px] bg-indigo-500 hover:bg-indigo-600 p-2 text-white transition-colors"
          >
            Get Started
          </Link>
          <Link
            to="/Login"
            className="border border-gray-700 rounded p-2 hover:bg-gray-800"
          >
            Login
          </Link>
        </div>
      </div>
    </div>
  )
}

export default HomeNav

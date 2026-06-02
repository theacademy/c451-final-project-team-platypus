import React from 'react'
import HomeNav from '../components/HomeNav'
import Footer from '../components/Footer'
import { Link } from 'react-router-dom'
import HomeLaptop from '../assets/Home_Laptop_Edit.png'

const Home = () => {
  return (
    <div className="flex flex-col min-h-svh">
      <HomeNav />
      <div className="bg-gray-950 flex-grow flex flex-col">
        <div className="container mx-auto flex bg-gray-950 flex-grow">
          {/* content */}
          <div className="flex flex-col p-10 pl-20 flex-grow mt-10">
            <div className="font-bold text-[42px] mt-30 text-white">
              Stock Market Simulator
            </div>
            <div className="text-[24px] text-gray-300">
              Practice investing with zero real-world risk
            </div>
            <div className="mt-20">
              <div className="text-[18px] pb-5 text-gray-400">
                The Platypus stock simulator is Free to join and use
              </div>
              <Link
                to="/Register"
                className="rounded-[20px] bg-indigo-500 hover:bg-indigo-600 p-3 text-[18px] text-white transition-colors"
              >
                Get Started
              </Link>
            </div>
          </div>
          {/* image */}
          <div className="flex p-2 flex-grow justify-center mt-10">
            <div className="p-15">
              <img
                src={HomeLaptop}
                className="h-100 w-auto mt-12"
                alt="Stock dashboard on a laptop"
              />
            </div>
          </div>
        </div>
      </div>
      <Footer />
    </div>
  )
}

export default Home

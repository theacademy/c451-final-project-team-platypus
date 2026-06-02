import React from 'react'
import HomeNav from '../../components/HomeNav'
import Footer from '../../components/Footer'
import MultiDevice from '../../assets/Multi_Device.png'
import { useState } from 'react'
import axios from 'axios'
import { Link, useNavigate } from 'react-router-dom'
import { saveUser } from '../../lib/session'

const Login = () => {
  let [userName, setUserName] = useState('')
  let [password, setPassword] = useState('')
  let [showPassword, setShowPassword] = useState(false)
  let [error, setError] = useState('')
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError('')

    try {
      // Username-only auth: password is collected but not sent (no password column).
      const res = await axios.post('/api/users/login', { userName })
      saveUser(res.data)
      navigate('/Dashboard')
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 404) {
        setError('No account found with that username.')
      } else {
        setError('Login failed. Please try again.')
      }
      console.error(err)
    }
  }

  return (
    <div className="flex flex-col min-h-svh bg-black">
      <HomeNav />
      <div className="bg-gray-950 flex-grow flex flex-col">
        <div className="container mx-auto flex flex-grow">
          {/* image */}
          <div className="flex-1 flex-col justify-center p-10 bg-gray-700 text-white flex-grow">
            <div className="text-[32px] mb-5 pt-5 mt-5">
              Unlock the Power of Investing
            </div>
            <ul className="list-disc pl-5 space-y-1 pt-3">
              <li>
                Practice trading stocks, cryptos and options with virtual money
              </li>
              <li>Gain confidence before risking your own money</li>
              <li>Learn how the market works in a safe space with no risk</li>
            </ul>
            <div className="mt-10 pl-10">
              <img src={MultiDevice} className="h-auto w-100" alt="" />
            </div>
          </div>
          {/* content */}
          <div className="flex-1 bg-gray-950 p-10 text-white flex-grow">
            <div className="text-[32px] mb-5 pt-5 mt-5">Login</div>
            <div className="text-[24px]">Welcome Back</div>
            <div className="flex gap-1">
              <div className="opacity-50">Don't have an account?</div>
              <Link
                to="/Register"
                className="font-bold text-indigo-500 underline hover:text-indigo-700 hover:no-underline duration-250 mb-10"
              >
                sign up here
              </Link>
            </div>
            <form className="flex flex-col" onSubmit={handleSubmit}>
              <label className="flex flex-col gap-2">
                Username
                <input
                  className="rounded-lg p-3 bg-gray-700 mb-5"
                  value={userName}
                  onChange={(e) => setUserName(e.target.value)}
                  placeholder="Enter username"
                  type="text"
                />
              </label>

              {/* <label
                htmlFor="current-password"
                className="block text-sm mb-2 dark:text-white"
              >
                Password
              </label>
              <input
                id="current-password"
                type={showPassword ? 'text' : 'password'}
                className="rounded-lg p-3 bg-gray-700 mb-5"
                placeholder="Enter password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              /> */}

              {/* Checkbox */}
              {/* <div className="flex mb-4">
                <input
                  id="show-password-checkbox"
                  type="checkbox"
                  checked={showPassword}
                  onChange={(e) => setShowPassword(e.target.checked)}
                  className="mt-0.5 border-gray-200 rounded-sm text-blue-600 mb-3"
                />
                <label
                  htmlFor="show-password-checkbox"
                  className="text-sm text-gray-500 ms-3 dark:text-neutral-400"
                >
                  Show password
                </label>
              </div> */}

              {error && (
                <div className="mb-4 text-red-400 text-sm">{error}</div>
              )}

              <label className="flex flex-col pt-1">
                <button
                  type="submit"
                  className="text-white bg-indigo-500 rounded-lg p-3 mb-5 hover:bg-indigo-700 duration-250"
                >
                  Sign In
                </button>
              </label>
            </form>
          </div>
        </div>
      </div>
      <Footer />
    </div>
  )
}

export default Login

import React from 'react'
import HomeNav from '../../components/HomeNav'
import Footer from '../../components/Footer'
import MultiDevice from '../../assets/Multi_Device.png'
import { useState } from 'react'
import axios from 'axios'
import { Link } from 'react-router-dom'

const Register = () => {
  let [email, setEmail] = useState('')
  let [password, setPassword] = useState('')
  let [showPassword, setShowPassword] = useState(false)

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()

    try {
      await axios.post('/api/users', {
        email,
        password,
      })
    } catch (error) {
      console.error(error)
    }
  }

  return (
    <div className="flex flex-col min-h-svh bg-black">
      <HomeNav />
      <div className="bg-gray-950 flex-grow flex flex-col">
        <div className="container mx-auto flex flex-grow">
          {/* image */}
          <div className="flex-1 flex-col justify-center p-10 bg-gray-700 text-white flex-grow">
            <div className="text-[32px] mb-5 pt-5 mt-5">Sign up for free</div>
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
            <div className="text-[32px] mb-5 pt-5 mt-5">Register</div>
            <div className="text-[24px]">Create an Account</div>
            <div className="flex gap-1">
              <div className="opacity-50">Already have an account?</div>
              <Link
                to="/Login"
                className="font-bold text-indigo-500 underline hover:text-indigo-700 hover:no-underline duration-250 mb-10"
              >
                log in here
              </Link>
            </div>
            <form className="flex flex-col" onSubmit={handleSubmit}>
              <label className="flex flex-col gap-2">
                Email
                <input
                  className="rounded-lg p-3 bg-gray-700 mb-5"
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="Enter email"
                  type="text"
                />
              </label>

              <label
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
              />

              {/* Checkbox */}
              <div className="flex mb-4">
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
              </div>
              {/* <div className="mb-5 text-gray-400">
                By signing up, you agree to our Terms of Service and Privacy
                Policy.
              </div> */}
              <label className="flex flex-col">
                <button
                  type="submit"
                  className="text-white bg-indigo-500 rounded-lg p-3 mb-5 hover:bg-indigo-700 duration-250"
                >
                  Sign Up
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

export default Register

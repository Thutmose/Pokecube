local component = require("component")
local splicer = component.splicer

local arg = {...}

local num = arg[1]

if num == nil then 
	num = 0
end
local name = splicer.getInfo(tonumber(num),0)
local args = splicer.getInfo(tonumber(num),1)

if args == nil then
	print("No Item in slot "..num)
	return
end
print("Name: "..name)
print()

local speed = math.floor((args - 256^4)/256^5)
args = args - speed * 256 ^ 5
local spdef = math.floor((args - 256^3)/256^4)
args = args - spdef * 256 ^ 4
local spatk = math.floor((args - 256^2)/256^3)
args = args - spatk * 256 ^ 3
local def = math.floor((args - 256^1)/256^2)
args = args - def * 256 ^ 2
local atk = math.floor((args - 256^0)/256^1)
args = args - atk * 256 ^ 1
local hp = math.floor(args)

print("IVs:")
print("Speed: "..speed)
print("Special Defense: "..spdef)
print("Special Attack: "..spatk)
print("Defense: "..def)
print("Attack: "..atk)
print("HP: "..hp)

args = splicer.getInfo(tonumber(num),2)
print()
print("Size: "..args)

local natures = {}

natures[0] = "HARDY"
natures[1] = "LONELY"
natures[2] = "BRAVE"
natures[3] = "ADAMANT"
natures[4] = "NAUGHTY"
natures[5] = "BOLD"
natures[6] = "DOCILE"
natures[7] = "RELAXED"
natures[8] = "IMPISH"
natures[9] = "LAX"
natures[10] = "TIMID"
natures[11] = "HASTY"
natures[12] = "SERIOUS"
natures[13] = "JOLLY"
natures[14] = "NAIVE"
natures[15] = "MODEST"
natures[16] = "MILD"
natures[17] = "QUIET"
natures[18] = "BASHFUL"
natures[19] = "RASH"
natures[20] = "CALM"
natures[21] = "GENTLE"
natures[22] = "SASSY"
natures[23] = "CAREFUL"
natures[24] = "QUIRKY"

args = splicer.getInfo(tonumber(num),3)
print()
print("Nature: "..natures[args])

args = splicer.getInfo(tonumber(num),4)
print()
print("Egg Moves")
for k,v in pairs(args) do print("   "..v) end

args = splicer.getInfo(tonumber(num),5)
print()
print("Shiny: "..tostring(args))
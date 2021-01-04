package main

import (
	"fmt"
	"image/png"
	"os"
	"path/filepath"
)

func main() {
	fnames, err := filepath.Glob("*.png")
	if err != nil {
		panic(err)
	}

	fmt.Println("fire_pixels = {")
	for _, fname := range fnames {
		dark, light := pixelToLua(fname)
		fmt.Printf("{dark={%s},light={%s}},\n", dark, light)
	}
	fmt.Println("}")
}

func pixelToLua(fname string) (string, string) {
	f, err := os.Open(fname)
	if err != nil {
		panic(err)
	}
	img, err := png.Decode(f)
	if err != nil {
		panic(err)
	}

	current := ""
	other := ""
	for x := img.Bounds().Min.X; x <= img.Bounds().Max.X; x++ {
		for y := img.Bounds().Min.Y; y <= img.Bounds().Max.Y; y++ {
			c := img.At(x, y)
			a, b, e, d := c.RGBA()
			d = a + b + e + d
			if d == 65535 {
				current += fmt.Sprintf("{%d,%d},", x-9, y)
			} else if d > 0 {
				other += fmt.Sprintf("{%d,%d},", x-9, y)
			}
		}
	}

	return current[:len(current)-1], other[:len(other)-1]
}

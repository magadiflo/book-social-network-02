import { Component, Input } from '@angular/core';

@Component({
  selector: 'rating',
  standalone: true,
  imports: [],
  templateUrl: './rating.component.html',
  styleUrl: './rating.component.scss'
})
export class RatingComponent {

  @Input() rating: number = 0;
  public maxRating: number = 5;

  public get fullStarts(): number {
    return Math.floor(this.rating);
  }

  public get hasHalfStart(): boolean {
    return this.rating % 1 !== 0;
  }

  public get emptyStarts(): number {
    return this.maxRating - Math.ceil(this.rating);
  }
}
